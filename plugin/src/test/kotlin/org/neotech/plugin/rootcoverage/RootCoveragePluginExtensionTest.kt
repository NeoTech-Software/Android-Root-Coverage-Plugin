package org.neotech.plugin.rootcoverage

import org.junit.Test
import kotlin.test.assertEquals

class RootCoveragePluginExtensionTest {

    @Test
    fun `default setting`() {
        val config = RootCoveragePluginExtension()
        assertEquals(true, config.includeUnitTestResults)
        assertEquals(true, config.includeAndroidTestResults)
        assertEquals(true, config.shouldExecuteUnitTests())
        assertEquals(true, config.shouldExecuteAndroidTests())
    }

    @Test
    fun `executeTests=false overrules execute(Unit or Android)Tests`() {
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
    fun `executeTests=true does not overrule execute(Unit or AndroidInstrumented)Tests`() {
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