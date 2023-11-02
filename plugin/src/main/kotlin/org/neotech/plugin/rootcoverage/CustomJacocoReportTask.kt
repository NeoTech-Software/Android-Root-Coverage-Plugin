package org.neotech.plugin.rootcoverage

import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.fileTree

abstract class CustomJacocoReportTask : JacocoReport() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:Input
    abstract val excludePatterns: ListProperty<String>

    fun allDirectories(): ListProperty<Directory> {
        val files = project.objects.listProperty(Directory::class.java)

        val filteredFiles: Provider<List<ConfigurableFileTree>> = files.map {
            it.map { directory ->
                project.fileTree(directory.asFile, excludes = excludePatterns.get())
            }
        }
        classDirectories.from(filteredFiles)
        return files
    }
}