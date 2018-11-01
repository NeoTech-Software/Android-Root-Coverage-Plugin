package org.neotech.plugin.rootcoverage

import com.android.builder.model.TestVariantBuildOutput

open class RootCoveragePluginExtension {

    var buildVariant: String = "debug"
    var buildVariantOverrides: Map<String, String> = mutableMapOf()
    var excludes: List<String> = mutableListOf()
    var skipTestExecution: Boolean = false
    var testTypes: List<TestVariantBuildOutput.TestType> = mutableListOf(TestVariantBuildOutput.TestType.ANDROID_TEST, TestVariantBuildOutput.TestType.UNIT)
}