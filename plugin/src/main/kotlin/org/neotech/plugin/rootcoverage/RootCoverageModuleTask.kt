package org.neotech.plugin.rootcoverage

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles

/**
 * This Task currently does not really do anything, except for having dependencies on the submodules
 * test tasks: unit and instrumented tests. It also has fields for storing the executionData,
 * sourceDirectories and classDirectories for the given submodule.
 */
open class RootCoverageModuleTask: DefaultTask() {

    @InputFiles
    var executionData: FileCollection = project.files()

    @InputFiles
    var sourceDirectories: FileCollection = project.files()

    @InputFiles
    var classDirectories: FileCollection = project.files()

}