package org.neotech.plugin.rootcoverage

import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.util.PatternSet
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.fileTree

abstract class CustomJacocoReportTask : JacocoReport() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:Input
    abstract val excludePatterns: ListProperty<String>

    fun allDirectories(): ListProperty<Directory> {
        val files = project.objects.listProperty(Directory::class.java)

        val filteredFiles: Provider<List<FileTree>> = files.map {
            val patternSet = PatternSet()
            patternSet.exclude(excludePatterns.get())
            it.map { directory ->
                directory.asFileTree.matching(patternSet)
            }
        }
        classDirectories.from(filteredFiles)
        return files
    }
}