package com.ms.square.plugins.gradle.glsl

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
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
    public void pluginWorksWithAndroidLibraryPlugin() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'android-library'
        project.apply plugin: 'glsl'
    }

    @Test
    public void pluginFailsWithoutAndroidPlugin() {
        Project project = ProjectBuilder.builder().build()
        try {
            project.apply plugin: 'glsl'
        } catch(ProjectConfigurationException e) {
            assertTrue(e.getMessage().equals("The android or android-library plugin must be applied to the project"));
        }
    }
}