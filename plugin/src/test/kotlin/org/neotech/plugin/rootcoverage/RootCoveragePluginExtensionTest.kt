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
}