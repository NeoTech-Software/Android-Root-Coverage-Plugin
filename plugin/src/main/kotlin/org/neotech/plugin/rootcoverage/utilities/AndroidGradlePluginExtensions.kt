package org.neotech.plugin.rootcoverage.utilities

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin

fun AndroidComponentsExtension<*, *, *>.assertMinimumRequiredAGPVersion(requiredVersion: AndroidPluginVersion) {
    if (pluginVersion < requiredVersion) {
        throw GradleException(
            "This version of the RootCoveragePlugin requires a minimum Android Gradle Plugin version of $requiredVersion"
        )
    }
}

fun Project.onVariant(variantName: String, action: (variant: Variant?) -> Unit){
    val androidComponents = extensions.getByType(AndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
        if (variant.name.replaceFirstChar(Char::titlecase) == variantName.replaceFirstChar(Char::titlecase)) {
            afterEvaluate {
                action(variant)
            }
        }
    }
}

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