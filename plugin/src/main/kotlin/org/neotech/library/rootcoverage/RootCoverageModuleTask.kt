package org.neotech.library.rootcoverage

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection

/**
 * This Task currently does not really do anything, except for having dependencies on the submodules
 * test tasks: unit and instrumented tests. It also has fields for storing the executionData,
 * sourceDirectories and classDirectories for the given submodule.
 */
open class RootCoverageModuleTask: DefaultTask() {

    var executionData: FileCollection = project.files()
    var sourceDirectories: FileCollection = project.files()
    var classDirectories: FileCollection = project.files()

}