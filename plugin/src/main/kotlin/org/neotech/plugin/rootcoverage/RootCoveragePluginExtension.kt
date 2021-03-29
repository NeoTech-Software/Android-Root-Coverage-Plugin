package org.neotech.plugin.rootcoverage

import com.android.builder.model.TestVariantBuildOutput
import org.gradle.api.Project
import org.slf4j.LoggerFactory

open class RootCoveragePluginExtension {

    var generateCsv: Boolean = false
    var generateHtml: Boolean = true
    var generateXml: Boolean = false

    var buildVariant: String = "debug"
    var buildVariantOverrides: Map<String, String> = mutableMapOf()
    var excludes: List<String> = mutableListOf()
    
    var includeNoLocationClasses: Boolean = false

    /**
     * Same as executeTests inverted.
     *
     * @see executeTests
     */
    @Deprecated("Please use `executeTests` instead.")
    var skipTestExecution: Boolean
        set(value) {
            executeTests = !value
        }
        get() = !executeTests

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

    @Deprecated("This setting has been replaced with `includeUnitTestResults` and `includeAndroidTestResults`.")
    var testTypes: List<TestVariantBuildOutput.TestType> =
        mutableListOf(TestVariantBuildOutput.TestType.ANDROID_TEST, TestVariantBuildOutput.TestType.UNIT)

    @Suppress("DEPRECATION")
    internal fun shouldExecuteAndroidTests() = executeTests && executeAndroidTests && includeAndroidTestResults()

    @Suppress("DEPRECATION")
    internal fun shouldExecuteUnitTests() = executeTests && executeUnitTests && includeUnitTestResults()

    @Suppress("DEPRECATION")
    internal fun includeAndroidTestResults(): Boolean {
        return if (!testTypes.contains(TestVariantBuildOutput.TestType.ANDROID_TEST)) {
            if (includeAndroidTestResults) {
                LoggerFactory.getLogger(Project::class.java).warn(
                    "Warning: Inconsistent settings, `testTypes` does not include TestType.ANDROID_TEST but " +
                            "`includeAndroidTestResults` is true. The setting `includeAndroidTestResults` will be ignored, to fix this " +
                            "issue please do not use the deprecated `testTypes` setting!"
                )
            }
            false
        } else {
            includeAndroidTestResults
        }
    }

    @Suppress("DEPRECATION")
    internal fun includeUnitTestResults(): Boolean {
        return if (!testTypes.contains(TestVariantBuildOutput.TestType.UNIT)) {
            if (includeUnitTestResults) {
                LoggerFactory.getLogger(Project::class.java).warn(
                    "Warning: Inconsistent settings, `testTypes` does not include TestType.UNIT but " +
                            "`includeUnitTestResults` is true. The setting `includeUnitTestResults` will be ignored, to fix this issue " +
                            "please do not use the deprecated `testTypes` setting!"
                )
            }
            false
        } else {
            includeUnitTestResults
        }
    }
}
