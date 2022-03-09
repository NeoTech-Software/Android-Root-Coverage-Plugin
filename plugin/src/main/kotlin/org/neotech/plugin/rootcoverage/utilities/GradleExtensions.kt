package org.neotech.plugin.rootcoverage.utilities

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree

/**
 * Executes an include match on the FileTree that only includes files with the .class extension.
 */
fun FileTree.excludeNonClassFiles(): FileTree = matching { it.include("**/*.class") }

/**
 * Wrapper around Project.fileTree(Map<String, ?>) to use it easier from Kotlin code.
 * Currently only supports the dir, excludes and includes properties.
 * */
fun Project.fileTree(dir: Any, excludes: List<String> = listOf(), includes: List<String> = listOf()): ConfigurableFileTree =
        fileTree(mapOf(
                "dir" to dir,
                "excludes" to excludes,
                "includes" to includes))
