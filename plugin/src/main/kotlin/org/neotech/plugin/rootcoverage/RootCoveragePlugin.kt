package org.neotech.plugin.rootcoverage

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

@Suppress("unused")
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
            createCoverageTaskForRoot(it)
        }
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
        rootProjectExtension.buildVariantOverrides[project.path] ?: rootProjectExtension.buildVariant

    private fun getExecutionDataFileTree(project: Project): FileTree {
        val rootFolderPatterns = mutableListOf<String>()
        val buildFolderPatterns = mutableListOf<String>()
        if (rootProjectExtension.includeUnitTestResults()) {
            buildFolderPatterns.add("jacoco/test*UnitTest.exec")

            // TODO instead of hardcoding this, obtain the location from the test tasks, something like this:
            // tasks.withType(Test::class.java).all { testTask ->
            //     testTask.extensions.findByType(JacocoTaskExtension::class.java)?.apply {
            //         destinationFile
            //     }
            // }
            rootFolderPatterns.add("jacoco.exec")
        }
        if (rootProjectExtension.includeAndroidTestResults()) {
            // Android Build Tools Plugin 3.2
            buildFolderPatterns.add("outputs/code-coverage/connected/*coverage.ec")

            // Android Build Tools Plugin 3.3+
            buildFolderPatterns.add("outputs/code_coverage/*/connected/*coverage.ec")
        }

        return project.fileTree(project.buildDir, includes = buildFolderPatterns) +
                project.fileTree(project.projectDir, includes = rootFolderPatterns)
    }

    /**
     * Throws a GradleException if the given buildVariant is not found in the set. This method only
     * works correctly if used after the Gradle evaluation phase! Use it for example in Task.doFirst
     * or Task.doLast.
     */
    private fun <T : BaseVariant> assertVariantExists(set: DomainObjectSet<T>, buildVariant: String, project: Project) {
        set.find {
            it.name.capitalize() == buildVariant.capitalize()
        }
            ?: throw GradleException(
                "Build variant `$buildVariant` required for module `${project.name}` does not exist. Make sure to use" +
                        " a proper build variant configuration using rootCoverage.buildVariant and" +
                        " rootCoverage.buildVariantOverrides."
            )
    }

    private fun createSubProjectCoverageTask(subProject: Project) {
        // Aggregates jacoco results from the app sub-project and bankingright sub-project and generates a report.
        // The report can be found at the root of the project in /build/reports/jacoco, so don't look in
        // /app/build/reports/jacoco you will only find the app sub-project report there.
        val task = subProject.tasks.create("coverageReport", JacocoReport::class.java)
        task.group = "reporting"
        task.description = "Generates a Jacoco for this Gradle module."

        task.reports.html.isEnabled = rootProjectExtension.generateHtml
        task.reports.xml.isEnabled = rootProjectExtension.generateXml
        task.reports.csv.isEnabled = rootProjectExtension.generateCsv

        task.reports.html.destination = subProject.file("${subProject.buildDir}/reports/jacoco")
        task.reports.xml.destination = subProject.file("${subProject.buildDir}/reports/jacoco.xml")
        task.reports.csv.destination = subProject.file("${subProject.buildDir}/reports/jacoco.csv")

        // Add some run-time checks.
        task.doFirst {
            val extension = subProject.extensions.findByName("android")
            if (extension != null) {
                val buildVariant = getBuildVariantFor(subProject)
                when (extension) {
                    is LibraryExtension -> assertVariantExists(extension.libraryVariants, buildVariant, subProject)
                    is AppExtension -> assertVariantExists(extension.applicationVariants, buildVariant, subProject)
                }
            }
        }

        task.addSubProject(task.project)
    }

    private fun createCoverageTaskForRoot(project: Project) {
        // Aggregates jacoco results from the app sub-project and bankingright sub-project and generates a report.
        // The report can be found at the root of the project in /build/reports/jacoco, so don't look in
        // /app/build/reports/jacoco you will only find the app sub-project report there.

        val task = project.tasks.create("rootCoverageReport", JacocoReport::class.java)
        task.group = "reporting"
        task.description = "Generates a Jacoco report with combined results from all the subprojects."

        task.reports.html.isEnabled = rootProjectExtension.generateHtml
        task.reports.xml.isEnabled = rootProjectExtension.generateXml
        task.reports.csv.isEnabled = rootProjectExtension.generateCsv

        task.reports.html.destination = project.file("${project.buildDir}/reports/jacoco")
        task.reports.xml.destination = project.file("${project.buildDir}/reports/jacoco.xml")
        task.reports.csv.destination = project.file("${project.buildDir}/reports/jacoco.csv")

        // Add some run-time checks.
        task.doFirst {
            it.project.allprojects.forEach { subProject ->
                val extension = subProject.extensions.findByName("android")
                if (extension != null) {
                    val buildVariant = getBuildVariantFor(subProject)
                    when (extension) {
                        is LibraryExtension -> assertVariantExists(extension.libraryVariants, buildVariant, subProject)
                        is AppExtension -> assertVariantExists(extension.applicationVariants, buildVariant, subProject)
                    }
                }
            }
        }

        // Configure the root task with sub-tasks for the sub-projects.
        task.project.subprojects.forEach {
            it.afterEvaluate { subProject ->
                subProject.applyConfiguration()
                task.addSubProject(subProject)
                createSubProjectCoverageTask(subProject)
            }
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
        // Only Android Application and Android Library modules are supported for now.
        val extension = subProject.extensions.findByName("android")
        if (extension == null) {
            // TODO support java modules?
            subProject.logger.warn(
                "Note: Skipping code coverage for module '${subProject.name}', currently the" +
                        " RootCoveragePlugin does not yet support Java Library Modules."
            )
            return
        } else if (extension is com.android.build.gradle.FeatureExtension) {
            // TODO support feature modules?
            subProject.logger.warn(
                "Note: Skipping code coverage for module '${subProject.name}', currently the" +
                        " RootCoveragePlugin does not yet support Android Feature Modules."
            )
            return
        }

        // Get the exact required build variant for the current sub-project.
        val buildVariant = getBuildVariantFor(subProject)
        when (extension) {
            is LibraryExtension -> {
                extension.libraryVariants.all { variant ->
                    if (variant.buildType.isTestCoverageEnabled && variant.name.capitalize() == buildVariant.capitalize()) {
                        if (subProject.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
                            subProject.plugins.apply(JacocoPlugin::class.java)
                            subProject.logJacocoHasBeenApplied()
                        }
                        addSubProjectVariant(subProject, variant)
                    }
                }
            }
            is AppExtension -> {
                extension.applicationVariants.all { variant ->
                    if (variant.buildType.isTestCoverageEnabled && variant.name.capitalize() == buildVariant.capitalize()) {
                        if (subProject.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
                            subProject.plugins.apply(JacocoPlugin::class.java)
                            subProject.logJacocoHasBeenApplied()
                        }
                        addSubProjectVariant(subProject, variant)
                    }
                }
            }
        }
    }

    private fun JacocoReport.addSubProjectVariant(subProject: Project, variant: BaseVariant) {
        val name = variant.name.capitalize()

        // Gets the relative path from this task to the subProject
        val path = project.relativePath(subProject.path).removeSuffix(":")

        // Add dependencies to the test tasks of the subProject
        if (rootProjectExtension.shouldExecuteUnitTests()) {
            dependsOn("$path:test${name}UnitTest")
        }
        if (rootProjectExtension.shouldExecuteAndroidTests()) {
            dependsOn("$path:connected${name}AndroidTest")
        }

        // Collect the class files based on the Java Compiler output
        val javaClassOutput = variant.javaCompileProvider.get().outputs
        val javaClassTrees = javaClassOutput.files.map { file ->
            subProject.fileTree(file, excludes = getFileFilterPatterns()).excludeNonClassFiles()
        }

        // TODO: No idea how to dynamically get the kotlin class files output folder, so for now this is hardcoded.
        // TODO: For some reason the tmp/kotlin-classes folder does not use the variant.dirName property, for now we instead use the variant.name.
        val kotlinClassFolder = "${subProject.buildDir}/tmp/kotlin-classes/${variant.name}"
        subProject.logger.info("Kotlin class folder for variant '${variant.name}': $kotlinClassFolder")

        val kotlinClassTree =
            subProject.fileTree(kotlinClassFolder, excludes = getFileFilterPatterns()).excludeNonClassFiles()

        // getSourceFolders returns ConfigurableFileCollections, but we only need the base directory of each ConfigurableFileCollection.
        val sourceFiles = variant.getSourceFolders(SourceKind.JAVA).map { file -> file.dir }

        sourceDirectories.from(subProject.files(sourceFiles))
        classDirectories.from(subProject.files(javaClassTrees, kotlinClassTree))
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
            "Jacoco plugin was not found for project: '${project.name}', it has been applied automatically:" +
                    " ${project.buildFile}"
        )
    }
}