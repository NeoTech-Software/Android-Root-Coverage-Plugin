package org.neotech.plugin.rootcoverage.utilities

import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin

fun Project.afterAndroidPluginApplied(notFoundAction: () -> Unit = {}, action: (AppliedPlugin) -> Unit) {
    var didExecuteBlock = false
    pluginManager.withPlugin(ANDROID_APPLICATION_PLUGIN_ID) {
        didExecuteBlock = true
        action(it)
    }
    pluginManager.withPlugin(ANDROID_LIBRARY_PLUGIN_ID) {
        didExecuteBlock = true
        action(it)
    }
    afterEvaluate {
        if (!didExecuteBlock) {
            notFoundAction()
        }
    }
}

private const val ANDROID_APPLICATION_PLUGIN_ID = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"