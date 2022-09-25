package org.neotech.plugin.rootcoverage

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.dsl.BuildType
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.SourceKind
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.afterAndroidPluginApplied
import org.neotech.plugin.rootcoverage.utilities.assertMinimumRequiredAGPVersion
import org.neotech.plugin.rootcoverage.utilities.fileTree
import org.neotech.plugin.rootcoverage.utilities.onVariant
import java.io.File

class RootCoveragePlugin : Plugin<Project> {

    private val minimumRequiredAgpVersion = AndroidPluginVersion(7, 2).alpha(6)

    private lateinit var rootProjectExtension: RootCoveragePluginExtension

    override fun apply(project: Project) {
        if (project.rootProject !== project) {
            throw GradleException(
                "The RootCoveragePlugin cannot be applied to project '${project.name}' because it" +
                        " is not the root project. Build file: ${project.buildFile}"
            )
        }
        rootProjectExtension = project.extensions.create("rootCoverage", RootCoveragePluginExtension::class.java)

        // Always apply JaCoCo to the project this plugin is applied to.
        project.applyJacocoPluginIfRequired()

        project.afterEvaluate {
            it.applyConfiguration()
        }
        createCoverageTaskForRoot(project)
    }

    private fun Project.applyJacocoPluginIfRequired() {
        if (plugins.withType(JacocoPlugin::class.java).isEmpty()) {
            plugins.apply(JacocoPlugin::class.java)
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
                // Only log if a build.gradle file was found in the project directory, if not it could just be an empty project that holds
                // child-projects
                if (File(subProject.projectDir, Project.DEFAULT_BUILD_FILE).exists()) {
                    subProject.logger.info("Note: Skipping code coverage for module '${subProject.name}', reason: not an Android module.")
                }
            },
            action = {
                subProject.applyJacocoPluginIfRequired()
                addSubProjectInternal(subProject)
            }
        )
    }

    private fun JacocoReport.addSubProjectInternal(subProject: Project) {
        // Only Android modules are supported
        val androidComponents = subProject.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.assertMinimumRequiredAGPVersion(minimumRequiredAgpVersion)

        // Get the exact required build variant for the current sub-project.
        val buildVariant = rootProjectExtension.getBuildVariantFor(subProject)

        lateinit var buildTypes: NamedDomainObjectContainer<out BuildType>
        androidComponents.finalizeDsl { extension ->
            buildTypes = extension.buildTypes
        }
        androidComponents.onVariants { variant ->
            val buildType = buildTypes.find { it.name == variant.buildType }!!
            if (variant.name.replaceFirstChar(Char::titlecase) == buildVariant.replaceFirstChar(Char::titlecase)) {
                if (buildType.enableAndroidTestCoverage || buildType.enableUnitTestCoverage || buildType.isTestCoverageEnabled) {
                    addSubProjectVariant(subProject, variant, buildType)
                } else {
                    subProject.logger.info("Note: Skipping code coverage for module '${subProject.name}', reason: BuildType $buildType has enableAndroidTestCoverage, enableUnitTestCoverage and testCoverageEnabled set to false, at least one of these must be true for code coverage to work.")
                }
            }
        }
    }

    private fun JacocoReport.addSubProjectVariant(subProject: Project, variant: Variant, buildType: BuildType) {
        val name = variant.name.replaceFirstChar(Char::titlecase)

        // Gets the relative path from this task to the subProject
        val path = project.relativeProjectPath(subProject.path)

        // Add dependencies to the test tasks of the subProject
        if (rootProjectExtension.shouldExecuteUnitTests() && (buildType.enableUnitTestCoverage || buildType.isTestCoverageEnabled)) {
            dependsOn("$path:test${name}UnitTest")
        }
        if (rootProjectExtension.shouldExecuteAndroidTests() && (buildType.enableAndroidTestCoverage || buildType.isTestCoverageEnabled)) {
            dependsOn("$path:connected${name}AndroidTest")
        }

        sourceDirectories.from(variant.sources.java?.all)

        classDirectories.from(variant.artifacts.getAll(MultipleArtifact.ALL_CLASSES_DIRS).map {
            it.map { directory ->
                subProject.fileTree(directory.asFile, excludes = rootProjectExtension.getFileFilterPatterns())
            }
        })
        executionData.from(
            subProject.getExecutionDataFileTree(
                includeUnitTestResults = rootProjectExtension.includeUnitTestResults && (buildType.enableUnitTestCoverage || buildType.isTestCoverageEnabled),
                includeAndroidTestResults = rootProjectExtension.includeAndroidTestResults && (buildType.enableAndroidTestCoverage || buildType.isTestCoverageEnabled)
            )
        )
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

    /**
     * Throws a GradleException if the given Android buildVariant is not found in this project.
     */
    private fun Project.assertAndroidCodeCoverageVariantExists() {
        afterAndroidPluginApplied {
            val buildVariant = rootProjectExtension.getBuildVariantFor(this)
            onVariant(buildVariant) { variant ->
                if (variant == null) {
                    // TODO only throw if testCoverage is enabled
                    throw GradleException(
                        "Build variant `$buildVariant` required for module `${project.name}` does not exist. Make sure to use" +
                                " a proper build variant configuration using rootCoverage.buildVariant and" +
                                " rootCoverage.buildVariantOverrides."
                    )
                }
            }
        }
    }
}

