Glsl Gradle Plugin for Android [![Build Status](https://travis-ci.org/Manabu-GT/glsl-gradle-plugin.png?branch=master)](https://travis-ci.org/Manabu-GT/glsl-gradle-plugin)
-------------------------------

With this plugin, you can place your GLSL codes in android's res/raw folder and access them through the auto-generated
Java class as a string constant for compilation.
In addition, this plugin automatically deletes all the *.glsl files under res/raw folder when packaging APKs.
It should prevent others from reading .glsl files too easily just by unzipping the APKs.
Of course, note that putting your GLSL code as a string constant in Java class does not completely protect your GLSL codes.
If you think you need further protection, consider using encryption. (In most cases, this isn't worth your effort.)

Compatibility
-------------
Currently known to work with Gradle 1.12, Android Gradle Plugin 0.11.x, and Android Studio 0.6.x

Installation
-------------
A Glsl Gradle Plugin installation takes only less than 30 seconds. Installation consists of adding the following to your ***build.gradle*** file:

 1. Add the Plugin's Maven repository:

        maven { url 'http://Manabu-GT.github.com/glsl-gradle-plugin/mvn-repo' }

 2. Add plugin dependency:

        classpath 'com.ms-square:glsl-gradle-plugin:0.1.1'

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
            classpath 'com.android.tools.build:gradle:0.11.+'
            classpath 'com.ms-square:glsl-gradle-plugin:0.1.1'
        }
    }

    apply plugin: 'android'
    apply plugin: 'glsl'


Usage
-----
Put your shader code (xxx.glsl) into android's res/raw folder.
After installing and applying the plugin, all the shader code files under res/raw will be automatically removed from the generated APKs.
Within the code, you can access your shader codes through the auto-generated Glsl.java file.

Example:

    buildProgram(Glsl.PARTICLE_VERTEX_SHADER, Glsl.PARTICLE_FRAGMENT_SHADER);

In the above example, the variable names such as 'PARTICLE_VERTEX_SHADER' are determined solely based on the corresponding glsl file names.

FYI, the auto-generated Glsl.java file will look like the following code.

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

Note:
The Glsl.java file will be generated under build/generated/source/glsl directory.

Additional Parameters
---------------------
By default, the Gradle plugin will output Glsl.java under the package name specified in your AndroidManifest.xml.
However, your can customize this behavior by updating build.gradle.
The following example demonstrates how to achieve it.

    android {
        glslConfig {
            outputPackage "com.hoge.android.glsl"
        }
    }

Since Android Studio gives you an error message on the Editor if the specified package does not already exist,
the default package name is recommended in most cases.

## License

```
 Copyright 2014 Manabu Shimobe

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```