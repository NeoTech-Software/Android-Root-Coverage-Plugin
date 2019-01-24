package org.neotech.plugin.rootcoverage

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class IntegrationTest(
        private val projectRoot: File,
        // Used by Junit as the test name, see @Parameters
        @Suppress("unused") private val name: String) {

    @Test
    fun execute() {
        createLocalPropertiesFile(projectRoot)

        val runner = GradleRunner.create()
                .withProjectDir(projectRoot)
                .withPluginClasspath()
                // Without forwardOutput travis CI could timeout because not output will be reported
                // for a long time.
                .forwardOutput()
                .withArguments("clean", "rootCodeCoverageReport", "--stacktrace")

        // Expect no failure
        val result = runner.build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertEquals(result.task(":rootCodeCoverageReport")!!.outcome, TaskOutcome.SUCCESS)

        val report = CoverageReport.from(File(projectRoot, "build/reports/jacoco.csv"))

        report.assertFullCoverage("org.neotech.library.android", "LibraryAndroidJava")
        report.assertFullCoverage("org.neotech.library.android", "LibraryAndroidKotlin")
    }

    companion object {

        // This method is used by the JVM (Parameterized JUnit Runner)
        @Suppress("unused")
        @Parameterized.Parameters(name = "{1}")
        @JvmStatic
        fun parameters(): List<Array<Any>> {
            return File("src/test/test-fixtures")
                    .listFiles()
                    .filter { it.isDirectory }
                    .map {
                        arrayOf(it, it.name)
                    }
        }
    }
}