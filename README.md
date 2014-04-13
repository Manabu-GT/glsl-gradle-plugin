Glsl Gradle Plugin
-------------------

Installation
---------
A Glsl Gradle Plugin installation takes only less than 30 seconds. Installation consists of adding the following to your ***build.gradle*** file:

 1. Add the Plugin's Maven repository:

        maven { url 'http://Manabu-GT.github.com/glsl-gradle-plugin/mvn-repo' }

 2. Add plugin dependency:

        classpath 'com.ms.square.plugins.gradle:glsl-gradle-plugin:0.1.0'

 3. Apply plugin:

        apply plugin: 'glsl'

Complete Example
----------------
For convenience, here is a snippet of a complete ***build.gradle*** file, including the additions above.

    buildscript {
        repositories {
            mavenCentral()
            maven { url 'http://Manabu-GT.github.com/glsl-gradle-plugin/mvn-repo' }
        }

        dependencies {
            classpath 'com.android.tools.build:gradle:0.9.+'
            classpath 'com.ms.square.plugins.gradle:glsl-gradle-plugin:0.1.0'
        }
    }

    apply plugin: 'android'
    apply plugin: 'glsl'


Usage
-----
Put your shader code (xxx.glsl) into android's res/raw folder.
After installing the plugin, all the shader code files under res/raw will be automatically removed from the generated APKs.
Within the code, you can access your shader codes through the auto-generated Glsl.java file.

For example,

    buildProgram(Glsl.PARTICLE_VERTEX_SHADER, Glsl.PARTICLE_FRAGMENT_SHADER);

    /** Automatically generated file. DO NOT MODIFY */
    public final class Glsl {
        public static final String PARTICLE_FRAGMENT_SHADER = "#version 110\n" +
                "precision mediump float;\n" +
                "varying vec3 v_Color;\n" +
                "varying float v_ElapsedTime;\n" +
                "void main() {\n" +
                "    gl_FragColor = vec4(v_Color / v_ElapsedTime, 1.0);\n" +
                "}";

        public static final String PARTICLE_VERTEX_SHADER = "#version 110\n" +
                "uniform mat4 u_Matrix;\n" +
                "uniform float u_Time;\n" +
                "attribute vec3 a_Position;\n" +
                "attribute vec3 a_Color;\n" +
                "attribute vec3 a_DirectionVector;\n" +
                "attribute float a_ParticleStartTime;\n" +
                "varying vec3 v_Color;\n" +
                "varying float v_ElapsedTime;\n" +
                "void main() {\n" +
                "    v_Color = a_Color;\n" +
                "    v_ElapsedTime = u_Time - a_ParticleStartTime;\n" +
                "    vec3 currentPosition = a_Position + (a_DirectionVector * v_ElapsedTime);\n" +
                "    gl_Position = u_Matrix * vec4(currentPosition, 1.0);\n" +
                "    gl_PointSize = 10.0;\n" +
                "}";
    }

By default, Glsl.java belongs to the default package and it's generated under build/source/glsl.

Additional Parameters
---------------------
