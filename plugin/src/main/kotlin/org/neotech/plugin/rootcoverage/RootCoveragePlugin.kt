package org.neotech.plugin.rootcoverage

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.BuildType
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.afterAndroidPluginApplied
import org.neotech.plugin.rootcoverage.utilities.assertMinimumRequiredAGPVersion
import org.neotech.plugin.rootcoverage.utilities.onVariant
import java.io.File

class RootCoveragePlugin : Plugin<Project> {

    private val minimumRequiredAgpVersion = AndroidPluginVersion(8, 8, 0)

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
        val task = subProject.createJacocoReportTask(
            taskName = "coverageReport",
            taskGroup = "reporting",
            taskDescription = "Generates a Jacoco for this Gradle module.",
            rootProjectExtension = rootProjectExtension
        )
        // subProject.assertAndroidCodeCoverageVariantExists()
        task.addSubProject(task.project)
    }

    private fun createCoverageTaskForRoot(project: Project) {
        val rootCoverageTask = project.createJacocoReportTask(
            taskName = "rootCoverageReport",
            taskGroup = "reporting",
            taskDescription = "Generates a Jacoco report with combined results from all the subprojects.",
            rootProjectExtension = rootProjectExtension
        )

        project.allprojects.forEach { subProject ->
            subProject.assertAndroidCodeCoverageVariantExists()
        }

        // Configure the root task with sub-tasks for the sub-projects.
        rootCoverageTask.project.subprojects.forEach {
            it.afterEvaluate { subProject ->
                subProject.applyConfiguration()
            }
            rootCoverageTask.addSubProject(it)

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
        val androidComponents = subProject.extensions.findByType(ApplicationAndroidComponentsExtension::class.java) ?: subProject.extensions.findByType(LibraryAndroidComponentsExtension::class.java)

        if(androidComponents == null) {
            throw GradleException("No application or library extension found on project: ${project.path}!")
        }

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
        val variantName = variant.name.replaceFirstChar(Char::titlecase)

        // Gets the relative path from this task to the subProject
        val path = project.relativeProjectPath(subProject.path)

        // Add dependencies to the test tasks of the subProject
        if (rootProjectExtension.shouldExecuteUnitTests() && (buildType.enableUnitTestCoverage || buildType.isTestCoverageEnabled)) {
            dependsOn("$path:test${variantName}UnitTest")
        }

        val androidTestTask = getAndroidTestTask(subProject, variant)
        val runsOnGradleManagedDevices = androidTestTask.runsOnGradleManagedDevices

        if (rootProjectExtension.shouldExecuteAndroidTests() && (buildType.enableAndroidTestCoverage || buildType.isTestCoverageEnabled)) {
            dependsOn(androidTestTask.taskPath)
        } else {
            // If this plugin should not run instrumented tests on it's own, at least make sure it runs after those tasks (if they are
            // selected to run as well and exists).
            //
            // In theory we don't need to do this if `rootProjectExtension.includeAndroidTestResults` is false, so we could check that, but
            // it also does not hurt.
            mustRunAfter(androidTestTask.taskPath)
        }

        sourceDirectories.from(variant.sources.java?.all)

        val taskProvider = variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT).use(
            project.tasks.named(this.name) as TaskProvider<CustomJacocoReportTask>
        )
        taskProvider.toGet(
            ScopedArtifact.CLASSES,
            CustomJacocoReportTask::allJars,
            CustomJacocoReportTask::allDirectories,
        )

        executionData.from(
            subProject.getExecutionDataFileTree(
                includeUnitTestResults = rootProjectExtension.includeUnitTestResults && (buildType.enableUnitTestCoverage || buildType.isTestCoverageEnabled),
                includeConnectedDevicesResults = rootProjectExtension.includeAndroidTestResults && (buildType.enableAndroidTestCoverage || buildType.isTestCoverageEnabled) && !runsOnGradleManagedDevices,
                includeGradleManagedDevicesResults = rootProjectExtension.includeAndroidTestResults && (buildType.enableAndroidTestCoverage || buildType.isTestCoverageEnabled) && runsOnGradleManagedDevices
            )
        )
    }

    private class AndroidTestTask(
        val taskPath: String,
        val runsOnGradleManagedDevices: Boolean
    )

    private fun JacocoReport.getAndroidTestTask(subProject: Project, variant: Variant): AndroidTestTask {
        // Gets the relative path from this task to the subProject
        val path = project.relativeProjectPath(subProject.path)
        val variantName = variant.name.replaceFirstChar(Char::titlecase)


        // Attempt to run on instrumented tests, giving priority to the following devices in this order:
        // - A user provided Gradle Managed Device.
        // - All Gradle Managed Devices if any is available.
        // - All through ADB connected devices.
        val gradleManagedDevices = subProject.extensions.getByType(BaseExtension::class.java).testOptions.managedDevices.devices

        if (rootProjectExtension.runOnGradleManagedDevices && !rootProjectExtension.gradleManagedDeviceName.isNullOrEmpty()) {
            return AndroidTestTask(
                taskPath = "$path:${rootProjectExtension.gradleManagedDeviceName}${variantName}AndroidTest",
                runsOnGradleManagedDevices = true
            )
        } else if (rootProjectExtension.runOnGradleManagedDevices && gradleManagedDevices.isNotEmpty()) {
            return AndroidTestTask(
                taskPath = "$path:allDevices${variantName}AndroidTest",
                runsOnGradleManagedDevices = true
            )
        } else {
            return AndroidTestTask(
                taskPath = "$path:connected${variantName}AndroidTest",
                runsOnGradleManagedDevices = false
            )
        }
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

