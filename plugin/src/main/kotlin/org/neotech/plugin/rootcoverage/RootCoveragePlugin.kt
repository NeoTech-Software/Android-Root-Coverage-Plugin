package org.neotech.plugin.rootcoverage

import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.dsl.BuildType
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.afterAndroidPluginApplied
import org.neotech.plugin.rootcoverage.utilities.fileTree

class RootCoveragePlugin : Plugin<Project> {

    private lateinit var rootProjectExtension: RootCoveragePluginExtension

    override fun apply(project: Project) {
        if (project.rootProject !== project) {
            throw GradleException(
                "The RootCoveragePlugin cannot be applied to project '${project.name}' because it" +
                        " is not the root project. Build file: ${project.buildFile}"
            )
        }
        rootProjectExtension = project.extensions.create("rootCoverage", RootCoveragePluginExtension::class.java)

        if (project.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
            project.plugins.apply(JacocoPlugin::class.java)
            project.logJacocoHasBeenApplied()
        }

        project.afterEvaluate {
            it.applyConfiguration()
        }
        createCoverageTaskForRoot(project)
    }

    private fun getFileFilterPatterns(): List<String> = listOf(
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
    ) + rootProjectExtension.excludes

    private fun getBuildVariantFor(project: Project): String =
        rootProjectExtension.buildVariantOverrides[project.path]
            ?: rootProjectExtension.buildVariant

    private fun getExecutionDataFileTree(project: Project): FileTree {
        val buildFolderPatterns = mutableListOf<String>()
        if (rootProjectExtension.includeUnitTestResults()) {
            // TODO instead of hardcoding this, obtain the location from the test tasks, something like this:
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
        if (rootProjectExtension.includeAndroidTestResults()) {

            // These are legacy paths for older now unsupported AGP version, they are just here for
            // reference and are not added to prevent existing files from polluting results
            //
            // Android Build Tools Plugin 3.2
            // buildFolderPatterns.add("outputs/code-coverage/connected/*coverage.ec")
            //
            // Android Build Tools Plugin 3.3-7.0
            // buildFolderPatterns.add("outputs/code_coverage/*/connected/*coverage.ec")

            // Android Build Tools Plugin 7.1+
            buildFolderPatterns.add("outputs/code_coverage/*/connected/*/coverage.ec")
        }

        return project.fileTree(project.buildDir, includes = buildFolderPatterns)
    }

    /**
     * Throws a GradleException if the given Android buildVariant is not found in this project.
     */
    private fun Project.assertAndroidCodeCoverageVariantExists() {
        afterAndroidPluginApplied {
            val buildVariant = getBuildVariantFor(this)

            var didFindBuildVariant = false
            val androidComponents = extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants {

                // TODO check test coverage
                // buildType.isTestCoverageEnabled

                if (it.name.replaceFirstChar(Char::titlecase) == buildVariant.replaceFirstChar(Char::titlecase)) {
                    didFindBuildVariant = true
                }
            }
            afterEvaluate {
                if (!didFindBuildVariant) {
                    throw GradleException(
                        "Build variant `$buildVariant` required for module `${project.name}` does not exist. Make sure to use" +
                                " a proper build variant configuration using rootCoverage.buildVariant and" +
                                " rootCoverage.buildVariantOverrides."
                    )
                }
            }

        }
    }

    private fun createSubProjectCoverageTask(subProject: Project) {
        val task = subProject.tasks.create("coverageReport", JacocoReport::class.java)

        // Make sure to only read from the rootProjectExtension after the project has been evaluated
        subProject.afterEvaluate {
            task.reports.html.required.set(rootProjectExtension.generateHtml)
            task.reports.xml.required.set(rootProjectExtension.generateXml)
            task.reports.csv.required.set(rootProjectExtension.generateCsv)
        }

        // Make sure to configure this JacocoReport task after the JaCoCoPlugin itself has been fully applied, otherwise the JaCoCoPlugin
        // may override settings in configureJacocoReportsDefaults()
        // https://github.com/gradle/gradle/blob/c177053ff95a1582c7919befe67993e0f1677f53/subprojects/jacoco/src/main/java/org/gradle/testing/jacoco/plugins/JacocoPlugin.java#L211
        subProject.pluginManager.withPlugin("jacoco") {
            task.group = "reporting"
            task.description = "Generates a Jacoco for this Gradle module."

            task.reports.html.outputLocation.set(subProject.file("${subProject.buildDir}/reports/jacoco"))
            task.reports.xml.outputLocation.set(subProject.file("${subProject.buildDir}/reports/jacoco.xml"))
            task.reports.csv.outputLocation.set(subProject.file("${subProject.buildDir}/reports/jacoco.csv"))
        }

        //subProject.assertAndroidCodeCoverageVariantExists()

        task.addSubProject(task.project)
    }

    private fun createCoverageTaskForRoot(project: Project) {
        val task = project.tasks.create("rootCoverageReport", JacocoReport::class.java)

        // Make sure to only read from the rootProjectExtension after the project has been evaluated
        project.afterEvaluate {
            task.reports.html.required.set(rootProjectExtension.generateHtml)
            task.reports.xml.required.set(rootProjectExtension.generateXml)
            task.reports.csv.required.set(rootProjectExtension.generateCsv)
        }

        // Make sure to configure this JacocoReport task after the JaCoCoPlugin itself has been fully applied, otherwise the JaCoCoPlugin
        // may override settings in configureJacocoReportsDefaults()
        // https://github.com/gradle/gradle/blob/c177053ff95a1582c7919befe67993e0f1677f53/subprojects/jacoco/src/main/java/org/gradle/testing/jacoco/plugins/JacocoPlugin.java#L211
        project.pluginManager.withPlugin("jacoco") {
            task.group = "reporting"
            task.description = "Generates a Jacoco report with combined results from all the subprojects."
            task.reports.html.outputLocation.set(project.file("${project.buildDir}/reports/jacoco"))
            task.reports.xml.outputLocation.set(project.file("${project.buildDir}/reports/jacoco.xml"))
            task.reports.csv.outputLocation.set(project.file("${project.buildDir}/reports/jacoco.csv"))
        }

        project.allprojects.forEach { subProject ->
            subProject.assertAndroidCodeCoverageVariantExists()
        }

        // Configure the root task with sub-tasks for the sub-projects.
        task.project.subprojects.forEach {
            it.afterEvaluate { subProject ->
                subProject.applyConfiguration()
            }
            task.addSubProject(it)
            createSubProjectCoverageTask(it)
        }

        project.tasks.create("rootCodeCoverageReport").apply {
            doFirst {
                logger.warn(
                    "The rootCodeCoverageReport task has been renamed in favor of rootCoverageReport, please" +
                            " rename any references to this task."
                )
            }
            dependsOn("rootCoverageReport")
        }
    }

    private fun JacocoReport.addSubProject(subProject: Project) {
        subProject.afterAndroidPluginApplied(
            notFoundAction = {
                subProject.logger.warn("Note: Skipping code coverage for module '${subProject.name}', reason: not an Android module.")
            },
            action = {
                addSubProjectInternal(subProject)
            }
        )
    }

    private fun JacocoReport.addSubProjectInternal(subProject: Project) {
        // Only Android modules are supported
        val androidComponents = subProject.extensions.getByType(AndroidComponentsExtension::class.java)

        // Get the exact required build variant for the current sub-project.
        val buildVariant = getBuildVariantFor(subProject)


        lateinit var buildTypes: NamedDomainObjectContainer<out BuildType>
        androidComponents.finalizeDsl { extension ->
            buildTypes = extension.buildTypes
        }
        androidComponents.onVariants { variant ->
            val buildType = buildTypes.find { it.name == variant.buildType }!!
            if (buildType.isTestCoverageEnabled && variant.name.replaceFirstChar(Char::titlecase) == buildVariant.replaceFirstChar(Char::titlecase)) {
                if (subProject.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
                    subProject.plugins.apply(JacocoPlugin::class.java)
                    subProject.logJacocoHasBeenApplied()
                }
                addSubProjectVariant(subProject, variant)
            }
        }
    }

    private fun JacocoReport.addSubProjectVariant(subProject: Project, variant: Variant) {
        val name = variant.name.replaceFirstChar(Char::titlecase)

        // Gets the relative path from this task to the subProject
        val path = project.relativePath(subProject.path).removeSuffix(":")

        // Add dependencies to the test tasks of the subProject
        if (rootProjectExtension.shouldExecuteUnitTests()) {
            dependsOn("$path:test${name}UnitTest")
        }
        if (rootProjectExtension.shouldExecuteAndroidTests()) {
            dependsOn("$path:connected${name}AndroidTest")
        }

        sourceDirectories.from(variant.sources.java.all)
        classDirectories.from(variant.artifacts.getAll(MultipleArtifact.ALL_CLASSES_DIRS).map {
            it.map { directory ->
                subProject.fileTree(directory.asFile, excludes = getFileFilterPatterns())
            }
        })
        executionData.from(getExecutionDataFileTree(subProject))
    }

    /**
     * Apply configuration from [RootCoveragePluginExtension] to the project.
     */
    private fun Project.applyConfiguration() {
        tasks.withType(Test::class.java) { testTask ->
            testTask.extensions.findByType(JacocoTaskExtension::class.java)?.apply {
                isIncludeNoLocationClasses = rootProjectExtension.includeNoLocationClasses
                if (isIncludeNoLocationClasses) {
                    // This Plugin is used for Android development and should support the Robolectric + Jacoco use-case 
                    // flawlessly, therefore this "bugfix" is included in the plugin codebase:
                    // See: https://github.com/gradle/gradle/issues/5184#issuecomment-457865951
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }

    private fun Project.logJacocoHasBeenApplied() {
        project.logger.info(
            "Note: Jacoco plugin was not found for project '${project.name}', it has been applied automatically: ${project.buildFile}"
        )
    }
}

