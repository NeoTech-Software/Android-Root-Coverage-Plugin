package org.neotech.plugin.rootcoverage

import com.android.builder.model.TestVariantBuildOutput

open class RootCoveragePluginExtension {

    var generateCsv: Boolean = false
    var generateHtml: Boolean = true
    var generateXml: Boolean = false
    var buildVariant: String = "debug"
    var buildVariantOverrides: Map<String, String> = mutableMapOf()
    var excludes: List<String> = mutableListOf()
    var skipTestExecution: Boolean = false
    var testTypes: List<TestVariantBuildOutput.TestType> = mutableListOf(TestVariantBuildOutput.TestType.ANDROID_TEST, TestVariantBuildOutput.TestType.UNIT)
}
