package com.ms.square.plugins.gradle.glsl

import org.gradle.api.Project

class GlslExtension {

    // Package name for the generated class (Glsl.java)
    private String outputPackage

    GlslExtension(Project project) {

    }

    void outputPackage(String outputPackage) {
        this.outputPackage = outputPackage
    }

    String getOutputPackage() {
        return outputPackage
    }
}