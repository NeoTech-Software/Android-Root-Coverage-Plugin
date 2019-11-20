package org.neotech.plugin.rootcoverage

import com.android.builder.model.TestVariantBuildOutput
import org.junit.Test
import kotlin.test.assertEquals

class RootCoveragePluginExtensionTest {

    @Test
    fun `default setting`() {
        val config = RootCoveragePluginExtension()
        assertEquals(true, config.includeUnitTestResults())
        assertEquals(true, config.includeAndroidTestResults())
        assertEquals(true, config.shouldExecuteUnitTests())
        assertEquals(true, config.shouldExecuteAndroidTests())
    }

    @Test
    fun `non default testTypes overrules include(Unit|Android)TestResults`() {
        // testTypes overrules includeAndroidTestResults & includeUnitTestResults (when testTypes is not default)
        val config = RootCoveragePluginExtension().apply {
            includeAndroidTestResults = true
            includeUnitTestResults = true
            testTypes = listOf()
        }
        assertEquals(false, config.includeUnitTestResults())
        assertEquals(false, config.includeAndroidTestResults())
    }

    @Test
    fun `default testTypes does not overrule include(Unit|Android)TestResults`() {
        // when testTypes is default includeAndroidTestResults & includeUnitTestResults overrule testTypes
        val config = RootCoveragePluginExtension().apply {
            includeAndroidTestResults = false
            includeUnitTestResults = false
            testTypes = listOf(TestVariantBuildOutput.TestType.UNIT, TestVariantBuildOutput.TestType.ANDROID_TEST)
        }
        assertEquals(false, config.includeUnitTestResults())
        assertEquals(false, config.includeAndroidTestResults())
    }

    @Test
    fun `shouldExecute(Unit|Android)Tests() returns false when include(Unit|Android)TestResults() returns false`() {
        // When test results are not included into the final report (`include*TestResults`), running the tests does not make sense, therefor
        // make sure `skip*TestExecution` returns false when this is the case.
        val config = RootCoveragePluginExtension().apply {
            includeAndroidTestResults = false
            includeUnitTestResults = false
            testTypes = listOf(TestVariantBuildOutput.TestType.UNIT, TestVariantBuildOutput.TestType.ANDROID_TEST)
        }
        assertEquals(false, config.includeUnitTestResults())
        assertEquals(false, config.includeAndroidTestResults())

        assertEquals(false, config.shouldExecuteUnitTests())
        assertEquals(false, config.shouldExecuteAndroidTests())
    }

    @Test
    fun `executeTests=false overrules execute(Unit|Android)Tests`() {
        val config = RootCoveragePluginExtension().apply {
            executeTests = false
        }

        config.apply {
            executeAndroidTests = true
            executeUnitTests = true
        }
        assertEquals(false, config.shouldExecuteUnitTests())
        assertEquals(false, config.shouldExecuteAndroidTests())

        config.apply {
            executeAndroidTests = false
            executeUnitTests = false
        }
        assertEquals(false, config.shouldExecuteUnitTests())
        assertEquals(false, config.shouldExecuteAndroidTests())
    }

    @Test
    fun `executeTests=true does not overrule execute(Unit|AndroidInstrumented)Tests`() {
        val config = RootCoveragePluginExtension().apply {
            executeTests = true
        }

        config.apply {
            executeAndroidTests = false
            executeUnitTests = false
        }
        assertEquals(false, config.shouldExecuteUnitTests())
        assertEquals(false, config.shouldExecuteAndroidTests())

        config.apply {
            executeAndroidTests = true
            executeUnitTests = true
        }
        assertEquals(true, config.shouldExecuteUnitTests())
        assertEquals(true, config.shouldExecuteAndroidTests())
    }
}