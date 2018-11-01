package org.neotech.plugin.rootcoverage

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.io.OutputStreamWriter

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
                .forwardStdOutput(OutputStreamWriter(System.out))
                .withArguments("clean", "rootCodeCoverageReport", "--stacktrace")

        // Expect no failure
        val result = runner.build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertEquals(result.task(":rootCodeCoverageReport")!!.outcome, TaskOutcome.SUCCESS)
    }

    companion object {

        // This method is used by the JVM (Parameterized JUnit Runner)
        @Suppress("unused")
        @Parameters(name = "{1}")
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