package com.ms.square.plugins.gradle.glsl

import com.google.common.collect.Maps
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

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

    private static void generateGlsl(String packageName, String sourceOutputPath, String resOutputPath) {
        List<File> files = listGlslFromRawFolder(resOutputPath)
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

        File pkgFolder = new File(sourceOutputPath, packageName.replaceAll("\\.", File.separator));
        if (!pkgFolder.exists()) {
            pkgFolder.mkdirs();
        }

        File f = new File(pkgFolder, GLSL_FILENAME)
        f.write(template)
    }

    private static List<File> listGlslFromRawFolder(String resRawOutputPath) {
        List<File> files = new ArrayList<>()
        new File(resRawOutputPath).eachFileMatch(~/.*\.glsl/) { f ->
            files.add(f)
        }
        return files
    }

    private static void deleteFromRawFolder(Project project, String resRawOutputPath) {
        new File(resRawOutputPath).eachFileMatch(~/.*\.glsl/) { f ->
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

            def variants = null;
            if (project.plugins.findPlugin("android")) {
                //applicationVariants -> for application project
                variants = "applicationVariants";
            } else if (project.plugins.findPlugin("android-library")) {
                //libraryVariants -> for library project
                variants = "libraryVariants";
            } else {
                throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project", null)
            }

            project.logger.debug("BuildDir: ${project.buildDir}")

            tasks.whenTaskAdded { task ->

                project.android[variants].all { variant ->
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

                            String resRawOutputPath = "$project.buildDir/intermediates/res/${variantData.variantConfiguration.dirName}/raw"
                            String sourceOutputPath = "$project.buildDir/generated/source/glsl/${variantData.variantConfiguration.dirName}"

                            variant.javaCompile.options.compilerArgs += [
                                    '-sourcepath', new File(sourceOutputPath)
                            ]

                            project.logger.debug("PackageName: ${packageName}")
                            project.logger.debug("ResRawFolder: ${resRawOutputPath}")
                            project.logger.debug("SourceFolder: ${sourceOutputPath}")

                            File resRawOutputDir = new File(resRawOutputPath)
                            if (resRawOutputDir.exists()) {
                                generateGlsl(packageName, sourceOutputPath, resRawOutputPath)
                                deleteFromRawFolder(project, resRawOutputPath)
                            } else {
                                project.logger.warn("ResRawFolder not found: ${resRawOutputPath}")
                            }
                        }

                        project.(expectingTask.toString()).dependsOn(newTaskName)
                        // make it run after mergeDebugResources and mergeReleaseResources tasks
                        project.(newTaskName.toString()).mustRunAfter("merge${variant.name.capitalize()}Resources")
                    }
                }
            }
        }
    }
}