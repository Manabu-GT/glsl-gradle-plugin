package com.ms.square.plugins.gradle.glsl

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static junit.framework.TestCase.assertTrue

class GlslPluginTest {

    @Test
    public void pluginWorksWithAndroidPlugin() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'android'
        project.apply plugin: 'glsl'
    }

    @Test
    public void pluginFailsWithoutAndroidPlugin() {
        Project project = ProjectBuilder.builder().build()
        try {
            project.apply plugin: 'glsl'
        } catch(IllegalStateException e) {
            assertTrue(e.getMessage().equals("The 'android' plugin is required."));
        }
    }
}