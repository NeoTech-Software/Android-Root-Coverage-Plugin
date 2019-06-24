package org.neotech.plugin.rootcoverage

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.neotech.plugin.rootcoverage.util.SystemOutputWriter
import org.neotech.plugin.rootcoverage.util.createLocalPropertiesFile
import java.io.File
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class IntegrationTest(
        // Used by Junit as the test name, see @Parameters
        @Suppress("unused") private val name: String,
        private val projectRoot: File,
        private val gradleVersion: String) {

    @Test
    fun execute() {
        createLocalPropertiesFile(projectRoot)

        val runner = GradleRunner.create()
                .withProjectDir(projectRoot)
                .withGradleVersion(gradleVersion)
                .withPluginClasspath()
                // Without forwardOutput Travis CI could timeout (which happens when Travis receives
                // no output for more than 10 minutes)
                .forwardStdOutput(SystemOutputWriter.out())
                .forwardStdError(SystemOutputWriter.err())
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

        @Suppress("unused") // This method is used by the JVM (Parameterized JUnit Runner)
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): List<Array<Any>> {

            val testFixtures = File("src/test/test-fixtures").listFiles().filter { it.isDirectory }
            val gradleVersions = arrayOf("5.1.1", "5.2.1", "5.4.1")

            return testFixtures.flatMap { file ->
                gradleVersions.map { gradleVersion ->
                    arrayOf("${file.name}-$gradleVersion", file, gradleVersion)
                }
            }
        }
    }
}
