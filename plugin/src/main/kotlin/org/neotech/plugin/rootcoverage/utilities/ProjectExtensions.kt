package org.neotech.plugin.rootcoverage.utilities

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Returns the output report file composed from the given [fileName] as a [RegularFile].
 */
internal fun Project.getReportOutputFile(fileName: String): Provider<RegularFile> =
    layout.buildDirectory.file("reports/$fileName")

/**
 * Returns the output report path composed from the given [directory] as a [Directory].
 */
internal fun Project.getReportOutputDir(directory: String): Provider<Directory> =
    layout.buildDirectory.dir("reports/$directory")
