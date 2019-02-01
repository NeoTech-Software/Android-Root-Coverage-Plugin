package org.neotech.plugin.rootcoverage.util

import java.io.File
import java.util.*

/**
 * Creates the local.properties file if it does not exist in the given root, automatically tries to
 * add the sdk.dir property.
 */
internal fun createLocalPropertiesFile(root: File, properties: Properties = Properties()) {
    val androidHomeDirectory = androidHomeDirectory().absolutePath.replace('\\', '/')
    properties["sdk.dir"] = androidHomeDirectory
    File(root, "local.properties").outputStream().use { properties.store(it, null) }
}

/**
 * Creates the gradle.properties file if it does not exist in the given root
 */
internal fun createGradlePropertiesFile(root: File, properties: Properties) {
    File(root, "gradle.properties").outputStream().use { properties.store(it, null) }
}

/**
 * Tries to resolve the Android SDK directory. This function first tries the ANDROID_HOME system
 * environment variable if this variable does not exist it tries to find and parse the
 * local.properties file in the upper (current) project (based on the current Java working
 * directory).
 */
internal fun androidHomeDirectory(): File {
    System.getenv("ANDROID_HOME")?.let {
        return File(it)
    }
    val localPropertiesFile = File(File(System.getProperty("user.dir")).parentFile, "local.properties")
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use {
            properties.load(it)
        }
        properties.getProperty("sdk.dir")?.let {
            return File(it)
        }
    }
    throw IllegalStateException("Missing 'ANDROID_HOME' environment variable or local.properties with 'sdk.dir'")
}
