package com.ms.square.plugins.gradle.glsl

import com.google.common.collect.Maps
import org.gradle.api.Plugin
import org.gradle.api.Project

class GlslPlugin implements Plugin<Project> {

    private final static String SRC_TEMPLATE = "/** Automatically generated file. DO NOT MODIFY */\n" +
            "package #PACKAGE#;\n" +
            "\n" +
            "public final class Glsl {\n" +
            "\n" +
            "#ADDITIONAL_LINES#}"
    private final static String PH_PACKAGE = "#PACKAGE#"
    private final static String PH_LINES = "#ADDITIONAL_LINES#"

    private final static String GLSL_FILENAME = "Glsl.java"

    private static String readGlslFile(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file))
        StringBuilder sb = new StringBuilder()
        String line
        while((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                sb.append("\"").append(line).append("\\\\n").append("\"").append(" +").append("\n")
            }
        }
        sb.append("\"\"")
        return sb.toString()
    }

    private static void generateGlsl(String packageName, String sourceOutputDir, String resOutputDir) {
        List<File> files = listGlslFromRawFolder(resOutputDir)
        Map<String, String> params = Maps.newHashMap()
        params.put(PH_PACKAGE, packageName)

        StringBuilder builder = new StringBuilder()
        for (File file : files) {
            builder.append("    ").append("public static final String ").append(getBaseFileName(file.name.toUpperCase()))
                    .append(" = ").append(readGlslFile(file.absolutePath)).append(";").append('\n')
        }
        params.put(PH_LINES, builder.toString())

        String template = new String(SRC_TEMPLATE)
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue()
            if (value != null) {
                template = template.replaceAll(entry.getKey(), value)
            }
        }
        File parent = new File(sourceOutputDir, packageName)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        File f = new File(parent, GLSL_FILENAME)
        f.write(template)
    }

    private static List<File> listGlslFromRawFolder(String resRawOutputDir) {
        List<File> files = new ArrayList<>()
        new File(resRawOutputDir).eachFileMatch(~/.*\.glsl/) { f ->
            files.add(f)
        }
        return files
    }

    private static void deleteFromRawFolder(Project project, String resRawOutputDir) {
        new File(resRawOutputDir).eachFileMatch(~/.*\.glsl/) { f ->
            project.logger.debug("Deleting File:${f.name}")
            f.delete()
        }
    }

    private static String getBaseFileName(String fileName) {
        int index = fileName.lastIndexOf('.')
        if (index == -1) {
            return fileName
        } else {
            return fileName.substring(0, index)
        }
    }

    @Override
    void apply(Project project) {
        // create an extension where the settings reside
        def extension = project.extensions.create("glslConfig", GlslExtension, project)

        project.configure(project) {
            if (it.hasProperty("android")) {

                project.logger.debug("BuildDir: ${project.buildDir}")

                tasks.whenTaskAdded { task ->
                    //applicationVariants -> for application project
                    project.("android").applicationVariants.all { variant ->
                        // locate processDebugResources and processReleaseResources tasks
                        def expectingTask = "process${variant.name.capitalize()}Resources".toString()
                        def buildType = "${variant.buildType.name}"

                        project.logger.debug("BuildType: ${buildType}")

                        if (expectingTask.equals(task.name)) {
                            def variantName = variant.name
                            // create new task with name such as glslRelease and glslDebug
                            def newTaskName = "glsl${variantName.capitalize()}"

                            project.logger.debug("NewTaskName: ${newTaskName}")

                            project.task(newTaskName) << {
                                String packageName = project.glslConfig.getOutputPackage()
                                if (!packageName) {
                                    packageName = variant.getPackageName()
                                }
                                String resRawOutputDir = "$project.buildDir/res/all/${variantData.variantConfiguration.dirName}/raw"
                                String sourceOutputDir = "$project.buildDir/source/glsl/${variantData.variantConfiguration.dirName}"

                                project.logger.debug("PackageName: ${packageName}")
                                project.logger.debug("ResRawFolder: ${resRawOutputDir}")
                                project.logger.debug("SourceFolder: ${sourceOutputDir}")

                                generateGlsl(packageName, sourceOutputDir, resRawOutputDir)
                                deleteFromRawFolder(project, resRawOutputDir)
                            }

                            project.(expectingTask.toString()).dependsOn(newTaskName)
                            // make it run after mergeDebugResources and mergeReleaseResources tasks
                            project.(newTaskName.toString()).mustRunAfter("merge${variant.name.capitalize()}Resources")

                            // Add generated glsl directory into the source set for compilation
                            project.android.sourceSets.(buildType.toString()).java.srcDirs("$project.buildDir/source/glsl/$buildType")
                            project.logger.debug("android srcDirs($buildType): ${project.android.sourceSets.(buildType.toString()).java.getSrcDirs()}")
                        }
                    }
                }
            } else {
                throw new IllegalStateException("The 'android' plugin is required.")
            }
        }
    }
}