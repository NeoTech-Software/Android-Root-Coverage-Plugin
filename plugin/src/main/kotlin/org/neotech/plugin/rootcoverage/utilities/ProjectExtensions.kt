package org.neotech.plugin.rootcoverage.utilities

import org.gradle.api.Project
import java.io.File

/**
 * Returns the output report path composed from the given [fileName] as a [File].
 */
internal fun Project.getReportOutputFile(fileName: String): File = file("$buildDir/reports/$fileName")
