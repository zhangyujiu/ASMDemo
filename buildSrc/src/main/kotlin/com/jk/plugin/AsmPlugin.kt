package com.jk.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsmPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.getByType(AppExtension::class.java).registerTransform(AsmTransform())
    }
}