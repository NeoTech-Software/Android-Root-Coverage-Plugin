package org.neotech.plugin.rootcoverage

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.neotech.plugin.rootcoverage.utilities.fileTree

internal fun RootCoveragePluginExtension.getFileFilterPatterns(): List<String> = listOf(
    "**/AutoValue_*.*", // Filter to remove generated files from: https://github.com/google/auto
    //"**/*JavascriptBridge.class",

    // Android Databinding
    "**/*databinding",
    "**/*binders",
    "**/*layouts",
    "**/BR.class", // Filter to remove generated databinding files

    // Core Android generated class filters
    "**/R.class",
    "**/R$*.class",
    "**/Manifest*.*",
    "**/BuildConfig.class",
    "android/**/*.*",

    "**/*\$ViewBinder*.*",
    "**/*\$ViewInjector*.*",
    "**/Lambda$*.class",
    "**/Lambda.class",
    "**/*Lambda.class",
    "**/*Lambda*.class",
    "**/*\$InjectAdapter.class",
    "**/*\$ModuleAdapter.class",
    "**/*\$ViewInjector*.class"
) + excludes

internal fun RootCoveragePluginExtension.getBuildVariantFor(project: Project): String =
    buildVariantOverrides[project.path] ?: buildVariant

internal fun Project.getExecutionDataFileTree(includeUnitTestResults: Boolean, includeConnectedDevicesResults: Boolean, includeGradleManagedDevicesResults: Boolean): FileTree? {
    val buildFolderPatterns = mutableListOf<String>()
    if (includeUnitTestResults) {
        // TODO instead of hardcoding this, obtain the location from the test tasks, something like this?
        // tasks.withType(Test::class.java).all { testTask ->
        //     testTask.extensions.findByType(JacocoTaskExtension::class.java)?.apply {
        //         destinationFile
        //     }
        // }

        // These are legacy paths for older now unsupported AGP version, they are just here for
        // reference and are not added to prevent existing files from polluting results
        //
        // buildFolderPatterns.add("jacoco/test*UnitTest.exec")
        // rootFolderPatterns.add("jacoco.exec") // Note this is not a build folder pattern and is based off project.projectDir

        // Android Build Tools Plugin 7.0+
        buildFolderPatterns.add("outputs/unit_test_code_coverage/*/*.exec")
    }
    if (includeConnectedDevicesResults) {

        // These are legacy paths for older now unsupported AGP version, they are just here for
        // reference and are not added to prevent existing files from polluting results
        //
        // Android Build Tools Plugin 3.2
        // buildFolderPatterns.add("outputs/code-coverage/connected/*coverage.ec")
        //
        // Android Build Tools Plugin 3.3-7.0
        // buildFolderPatterns.add("outputs/code_coverage/*/connected/*coverage.ec")

        // Android Build Tools Plugin 7.1+
        buildFolderPatterns.add("outputs/code_coverage/*/connected/*/*.ec")
    }
    if(includeGradleManagedDevicesResults) {
        // Gradle Managed Devices 7.4
        // buildFolderPatterns.add("outputs/managed_device_code_coverage/*/coverage.ec")

        // Gradle Managed Devices 8.3+
        buildFolderPatterns.add("outputs/managed_device_code_coverage/*/*/*.ec")
        // In case of flavors coverage is nested an additional 2 folder deeper
        buildFolderPatterns.add("outputs/managed_device_code_coverage/*/flavors/*/*/*.ec")
    }
    return if(buildFolderPatterns.isEmpty()) {
        null
    } else {
        fileTree(layout.buildDirectory, includes = buildFolderPatterns)
    }
}