package org.neotech.plugin.rootcoverage

open class RootCoveragePluginExtension {

    var generateCsv: Boolean = false
    var generateHtml: Boolean = true
    var generateXml: Boolean = false

    var buildVariant: String = "debug"
    var buildVariantOverrides: Map<String, String> = mutableMapOf()
    var excludes: List<String> = mutableListOf()
    
    var includeNoLocationClasses: Boolean = false

    /**
     * Same as executeTests except that this only disables/enables the instrumented Android tests.
     *
     * Default: true
     *
     * @see executeTests
     */
    var executeAndroidTests: Boolean = true

    /**
     * Same as executeTests except that this only disables/enables the unit tests.
     *
     * Default: true
     *
     * @see executeTests
     */
    var executeUnitTests: Boolean = true

    /**
     * When disabled the Android-Root-Coverage-Plugin will skip the execution of all tests (unit and instrumented Android tests). This can
     * be useful when you run the tests manually or remote (Firebase Test Lab). When using this setting make sure you fetch the
     * `build/outputs` and `build/jacoco/` folders the remote (or any other place) and put them into the local build so that this plugin can
     * use them.
     *
     * Default: true
     *
     * Note: if false this will override any value in `executeAndroidTests` and `executeUnitTests`.
     *
     * @see executeAndroidTests
     * @see executeUnitTests
     */
    var executeTests: Boolean = true

    /**
     * Whether to include results from instrumented Android tests into the final coverage report. If disabled this also causes the plugin to
     * not automatically execute instrumented Android tests (if not already disabled by either `executeTests` or `executeAndroidTests`).
     *
     * Default: true
     *
     * @see includeUnitTestResults
     */
    var includeAndroidTestResults = true

    /**
     * Whether to include results from unit tests into the final coverage report. If disabled this also causes the plugin to not
     * automatically execute unit tests (if not already disabled by either `executeTests` or `executeUnitTests`).
     *
     * Default: true
     *
     * @see includeAndroidTestResults
     */
    var includeUnitTestResults = true

    internal fun shouldExecuteAndroidTests() = executeTests && executeAndroidTests && includeAndroidTestResults

    internal fun shouldExecuteUnitTests() = executeTests && executeUnitTests && includeUnitTestResults
}
