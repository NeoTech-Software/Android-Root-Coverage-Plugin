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
     * When disabled the plugin will skip the execution of all (instrumented) Android tests (by not depending on the Gradle test tasks).
     * If disabled this does not automatically imply that already existing code coverage results will not be included in the final coverage
     * report, instead this is expected to happen as this can be useful when you run the tests manually or remote (Firebase Test Lab).
     *
     * When using this setting in combination with remote test execution make sure you fetch the `build/outputs` folder from the remote (or
     * any other place) and put them into the local build so that this plugin can use them to configure code coverage correctly. Also make
     * sure that when executing remotely coverage is enabled.
     *
     * Same as executeUnitTests except that this only disables/enables the instrumented Android tests.
     *
     * Default: true
     *
     * @see executeTests
     */
    var executeAndroidTests: Boolean = true

    /**
     * When disabled the plugin will skip the execution of all unit tests (by not depending on the Gradle test tasks). If disabled this does
     * not automatically imply that already existing code coverage results will not be included in the final coverage report, instead this
     * is expected to happen as this can be useful when you run the tests manually or remote (Firebase Test Lab).
     *
     * When using this setting in combination with remote test execution make sure you fetch the `build/outputs` folder from the remote (or
     * any other place) and put them into the local build so that this plugin can use them to configure code coverage correctly. Also make
     * sure that when executing remotely coverage is enabled.
     *
     * Same as executeUnitTests except that this only disables/enables the instrumented Android tests.
     *
     * Default: true
     *
     * @see executeTests
     */
    var executeUnitTests: Boolean = true

    /**
     * @see executeAndroidTests
     * @see executeUnitTests
     */
    @Deprecated("Instead use executeAndroidTests and/or executeUnitTests")
    var executeTests: Boolean = true

    /**
     * Whether to include results from instrumented Android tests into the final coverage report. If disabled this also causes the plugin to
     * not automatically execute instrumented Android tests (if not already disabled by `executeAndroidTests`).
     *
     * Default: true
     *
     * @see includeUnitTestResults
     */
    var includeAndroidTestResults = true

    /**
     * Whether to include results from unit tests into the final coverage report. If disabled this also causes the plugin to not
     * automatically execute unit tests (if not already disabled by `executeUnitTests`).
     *
     * Default: true
     *
     * @see includeAndroidTestResults
     */
    var includeUnitTestResults = true

    /**
     * The name of the Gradle Managed device to run instrumented tests on, if null tests will be attempted to run on any
     * connected emulator.
     */
    var gradleManagedDeviceName: String? = null

    /**
     * If set to true (default is false) this plugin will attempt to run on Gradle Managed Devices before trying devices
     * connected through other means (Non Gradle Managed Devices).
     */
    var runOnGradleManagedDevices: Boolean = false
    
    internal fun shouldExecuteAndroidTests() = executeTests && executeAndroidTests && includeAndroidTestResults

    internal fun shouldExecuteUnitTests() = executeTests && executeUnitTests && includeUnitTestResults
}
