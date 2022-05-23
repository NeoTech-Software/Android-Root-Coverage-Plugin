package org.neotech.plugin.rootcoverage

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.GradleException
import org.junit.Test
import org.neotech.plugin.rootcoverage.utilities.assertMinimumRequiredAGPVersion

class AndroidGradlePluginExtensionsTest {

    @Test(expected = GradleException::class)
    fun `when AGP version is lower than minimum required then assertMinimumRequiredAGPVersion throws an exception`() {

        val mockedAndroidComponentsExtension = mockk<AndroidComponentsExtension<*,*,*>>()

        every { mockedAndroidComponentsExtension.pluginVersion } returns AndroidPluginVersion(7, 1)

        mockedAndroidComponentsExtension.assertMinimumRequiredAGPVersion(AndroidPluginVersion(7, 2).alpha(6))
    }

    @Test
    fun `when AGP version is higher than minimum required then assertMinimumRequiredAGAVEVersion() returns successfully`() {

        val mockedAndroidComponentsExtension = mockk<AndroidComponentsExtension<*,*,*>>()

        every { mockedAndroidComponentsExtension.pluginVersion } returns AndroidPluginVersion(7, 3)

        mockedAndroidComponentsExtension.assertMinimumRequiredAGPVersion(AndroidPluginVersion(7, 2).alpha(6))
    }
}