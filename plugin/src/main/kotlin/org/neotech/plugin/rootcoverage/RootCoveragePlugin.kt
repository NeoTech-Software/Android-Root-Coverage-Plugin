package org.neotech.plugin.rootcoverage

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import com.android.builder.model.TestVariantBuildOutput
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

@Suppress("unused")
class RootCoveragePlugin : Plugin<Project> {

    private lateinit var rootProjectExtension: RootCoveragePluginExtension;

    override fun apply(project: Project) {
        if (project.rootProject !== project) {
            throw GradleException("The RootCoveragePlugin can not be applied to project '${project.name}' because it is not the root project. Build file: ${project.buildFile}")
        }
        rootProjectExtension = project.extensions.create("rootCoverage", RootCoveragePluginExtension::class.java)

        if (project.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
            project.logger.warn("Warning: Jacoco plugin was not found for project: '${project.name}', it has been applied automatically but you should do this manually. Build file: ${project.buildFile}")
            project.plugins.apply(JacocoPlugin::class.java)
        }

        project.afterEvaluate { createCoverageTaskForRoot(it) }
    }

    private fun getFileFilterPatterns(): List<String> {
        return listOf(
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
                "**/*\$ViewInjector*.class") + rootProjectExtension.excludes
    }

    private fun getBuildVariantFor(project: Project): String {
        return rootProjectExtension.buildVariantOverrides[project.path]
                ?: rootProjectExtension.buildVariant
    }

    private fun getExecutionDataFilePatterns(): List<String> {
        val list = mutableListOf<String>()
        if (rootProjectExtension.testTypes.contains(TestVariantBuildOutput.TestType.UNIT)) {
            list.add("jacoco/test*UnitTest.exec")
        }
        if (rootProjectExtension.testTypes.contains(TestVariantBuildOutput.TestType.ANDROID_TEST)) {
            list.add("outputs/code-coverage/connected/*coverage.ec")
        }
        return list
    }

    /**
     * Throws a GradleException if the given buildVariant is not found in the set. This method only
     * works correctly if used after the Gradle evaluation phase! Use it for example in Task.doFirst
     * or Task.doLast.
     */
    private fun <T : BaseVariant> assertVariantExists(set: DomainObjectSet<T>, buildVariant: String, project: Project) {
        set.find {
            it.name.capitalize() == buildVariant.capitalize()
        } ?: throw GradleException("Build variant `$buildVariant` required for module `${project.name}` does not exist. Make sure to use a proper build variant configuration using rootCoverage.buildVariant and rootCoverage.buildVariantOverrides.")
    }

    private fun createCoverageTaskForRoot(project: Project) {
        // Aggregates jacoco results from the app sub-project and bankingright sub-project and generates a report.
        // The report can be found at the root of the project in /build/reports/jacoco, so don't look in
        // /app/build/reports/jacoco you will only find the app sub-project report there.
        val task = project.tasks.create("rootCodeCoverageReport", JacocoReport::class.java)
        task.group = "reporting"
        task.description = "Generates a Jacoco report with combined results from all the subprojects."

        task.reports.html.isEnabled = true
        task.reports.xml.isEnabled = false
        task.reports.csv.isEnabled = false
        task.reports.html.destination = project.file("${project.buildDir}/reports/jacoco")

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
        project.subprojects.forEach {
            it.afterEvaluate { subProject ->
                createCoverageTaskForSubProject(subProject, task)
            }
        }
    }

    private fun createCoverageTaskForSubProject(subProject: Project, task: JacocoReport) {
        // Only Android Application and Android Library modules are supported for now.
        val extension = subProject.extensions.findByName("android")
        if (extension == null) {
            // TODO support java modules?
            subProject.logger.warn("Note: Skipping code coverage for module '${subProject.name}', currently the RootCoveragePlugin does not yet support Java Library Modules.");
            return
        } else if (extension is com.android.build.gradle.FeatureExtension) {
            // TODO support feature modules?
            subProject.logger.warn("Note: Skipping code coverage for module '${subProject.name}', currently the RootCoveragePlugin does not yet support Android Feature Modules.")
            return
        }

        // Get the exact required build variant for the current sub-project.
        val buildVariant = getBuildVariantFor(subProject)
        when (extension) {
            is LibraryExtension -> {

                //assertVariantExists(extension.libraryVariants, buildVariant, subProject)

                extension.libraryVariants.all { variant ->

                    if (variant.buildType.isTestCoverageEnabled && variant.name.capitalize() == buildVariant.capitalize()) {
                        if (subProject.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
                            subProject.logger.warn("Warning: Jacoco plugin was not found for project: '${subProject.name}', it has been applied automatically but you should do this manually. Build file: ${subProject.buildFile}")
                            subProject.plugins.apply(JacocoPlugin::class.java)
                        }
                        val subTask = createTask(subProject, variant)
                        addSubTaskDependencyToRootTask(task, subTask)
                    }
                }
            }
            is AppExtension -> {

                //assertVariantExists(extension.libraryVariants, buildVariant, subProject)

                extension.applicationVariants.all { variant ->
                    if (variant.buildType.isTestCoverageEnabled && variant.name.capitalize() == buildVariant.capitalize()) {
                        if (subProject.plugins.withType(JacocoPlugin::class.java).isEmpty()) {
                            subProject.logger.warn("Jacoco plugin not applied to project: '${subProject.name}'! RootCoveragePlugin automatically applied it but you should do this manually: ${subProject.buildFile}")
                            subProject.plugins.apply(JacocoPlugin::class.java)
                        }
                        val subTask = createTask(subProject, variant)
                        addSubTaskDependencyToRootTask(task, subTask)
                    }
                }
            }
        }
    }

    private fun createTask(project: Project, variant: BaseVariant): RootCoverageModuleTask {
        //println("Create code coverage report for variant ${project.name} ${variant.name}")

        val name = variant.name.capitalize()

        val codeCoverageReportTask = project.tasks.register("codeCoverageReport$name", RootCoverageModuleTask::class.java)
        codeCoverageReportTask.configure { task ->
            task.group = null // null makes sure the group does not show in the gradle-view in Android Studio/Intellij
            task.description = "Generate unified Jacoco code codecoverage report"

            if (!rootProjectExtension.skipTestExecution) {
                if (rootProjectExtension.testTypes.contains(TestVariantBuildOutput.TestType.UNIT)) {
                    task.dependsOn("test${name}UnitTest")
                }
                if (rootProjectExtension.testTypes.contains(TestVariantBuildOutput.TestType.ANDROID_TEST)) {
                    task.dependsOn("connected${name}AndroidTest")
                }
            }

            // Collect the class files based on the Java Compiler output
            val javaClassOutputs = variant.javaCompileProvider.get().outputs
            val javaClassTrees = javaClassOutputs.files.map { file ->
                project.fileTree(file, excludes = getFileFilterPatterns()).excludeNonClassFiles()
            }
            // Hard coded alternative to get class files for Java.
            //val classesTree = project.fileTree(mapOf("dir" to "${project.buildDir}/intermediates/classes/${variant.dirName}", "excludes" to getFileFilterPatterns()))

            // TODO: No idea how to dynamically get the kotlin class files output folder, so for now this is hardcoded.
            // TODO: For some reason the tmp/kotlin-classes folder does not use the variant.dirName property, for now we instead use the variant.name.
            val kotlinClassFolder = "${project.buildDir}/tmp/kotlin-classes/${variant.name}"
            project.logger.info("Kotlin class folder for variant '${variant.name}': $kotlinClassFolder")

            val kotlinClassTree = project.fileTree(kotlinClassFolder, excludes = getFileFilterPatterns()).excludeNonClassFiles()

            // getSourceFolders returns ConfigurableFileCollections, but we only need the base directory of each ConfigurableFileCollection.
            val sourceFiles = variant.getSourceFolders(SourceKind.JAVA).map { file -> file.dir }

            task.sourceDirectories = project.files(sourceFiles)
            task.classDirectories = project.files(javaClassTrees, kotlinClassTree)
            task.executionData = project.fileTree(project.buildDir, includes = getExecutionDataFilePatterns())
        }

        return codeCoverageReportTask.get()
    }

    private fun addSubTaskDependencyToRootTask(rootTask: JacocoReport, subModuleTask: RootCoverageModuleTask) {

        // Make the root task depend on the sub-project code coverage task
        rootTask.dependsOn(subModuleTask)

        // Set or add the sub-task class directories to the root task
        if (rootTask.classDirectories == null) {
            rootTask.classDirectories.setFrom(subModuleTask.classDirectories)
        } else {
            rootTask.additionalClassDirs(subModuleTask.classDirectories)
        }

        // Set or add the sub-task source directories to the root task
        if (rootTask.sourceDirectories == null) {
            rootTask.sourceDirectories.setFrom(subModuleTask.sourceDirectories)
        } else {
            rootTask.additionalSourceDirs(subModuleTask.sourceDirectories)
        }

        // Add the sub-task class directories to the root task
        rootTask.executionData(subModuleTask.executionData)
    }
}
