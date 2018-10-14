package org.neotech.plugin.rootcoverage

open class RootCoveragePluginExtension {

    var buildVariant: String = "debug"
    var buildVariantOverrides: Map<String, String> = mutableMapOf()
    var excludes: List<String> = mutableListOf()
}