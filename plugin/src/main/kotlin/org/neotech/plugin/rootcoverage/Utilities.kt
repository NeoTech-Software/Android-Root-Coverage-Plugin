package org.neotech.plugin.rootcoverage

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree

/**
 * Executes an include match on the FileTree that only includes files with the .class extension.
 */
fun FileTree.excludeNonClassFiles(): FileTree {
    return matching {
        it.include("**/*.class")
    }
}

/**
 * Wrapper around Project.fileTree(Map<String, ?>) to use it easier from Kotlin code (no need to create the map every time).
 * Currently only supports the dir, excludes and includes properties.
 * */
fun Project.fileTree(dir: Any, excludes: List<String> = listOf(), includes: List<String> = listOf()): ConfigurableFileTree {
    return fileTree(mapOf(
            "dir" to dir,
            "excludes" to excludes,
            "includes" to includes))
}
