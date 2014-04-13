import com.google.common.collect.Maps
import org.gradle.api.Plugin
import org.gradle.api.Project

class GlslPlugin implements Plugin<Project> {

    private final static String TEMPLATE = "Glsl.template";
    private final static String PH_PACKAGE = "#PACKAGE#";
    private final static String PH_LINES = "#ADDITIONAL_LINES#";

    def glslRawDirPath = "app/src/main/res/raw"

    private String readTextFile(File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            result.append(line).append('\n');
        }
        return result.toString();
    }

    private void generateGlsl(String packageName, String sourceOutputDir) {
        List<File> files = listGlslFromRawFolder(getResOutputDir().absolutePath)
        Map<String, String> map = Maps.newHashMap()
        map.put(PH_PACKAGE, packageName)

        StringBuilder sb = new StringBuilder()

        for (File f : files) {
            String fileName = f.name
            println "FileName:" + fileName
            sb.append("    ").append("public final String ").append(fileName.capitalize())
                    .append(" = \"").append(readTextFile(f)).append("\";")append('\n');
        }

        map.put(PH_LINES, sb.toString())

    }

    private List<File> listGlslFromRawFolder(String resDirPath) {
        List<File> files = new ArrayList<>()
        new File(resDirPath).eachFileMatch("**/raw/.*glsl") { f ->
            files.add(f)
        }
        return files
    }

    private void deleteFromRawFolder(String resDirPath) {
        new File(resDirPath).eachFileMatch("**/raw/.*glsl") { f ->
            f.delete()
            println "File:" + f.name + " Deleted"
        }
    }

    @Override
    void apply(Project project) {
        // create an extension where the settings reside
        def extension = project.extensions.create("glslConfig", GlslExtension, project)

        project.configure(project) {
            if (it.hasProperty("android")) {
                tasks.whenTaskAdded { task ->
                    project.("android").applicationVariants.all { variant ->
                        // locate generateDebugSources and generateReleaseSources tasks
                        def expectingTask = "generate${variant.name.capitalize()}Sources".toString()

                        if (expectingTask.equals(task.name)) {
                            def variantName = variant.name
                            // create new task with name such as glslRelease and glslDebug
                            def newTaskName = "glsl${variantName.capitalize()}"

                            println "BuildDir:$project.buildDir"

                            project.task(newTaskName) << {
                                // must clear the folder in case the packagename changed, otherwise,
                                // there'll be two classes.
                                //File destinationDir = getSourceOutputDir()
                                //emptyFolder(destinationDir)

                                String packageName;
                                if (variant.getType() == VariantConfiguration.Type.TEST) {
                                    packageName = variant.getPackageName();
                                } else {
                                    packageName = variant.getPackageFromManifest();
                                }

                                println "PackageName:" + packageName

                                generateGlsl(packageName, getSourceOutputDir().absolutePath)
                                deleteFromRawFolder(getResOutputDir().absolutePath)
                            }

                            task.dependsOn(newTaskName)
                        }
                    }
                }
            }
        }
    }
}