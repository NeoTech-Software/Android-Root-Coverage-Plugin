package org.neotech.plugin.rootcoverage

import com.google.common.truth.Truth.assertThat
import groovy.text.SimpleTemplateEngine
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.neotech.plugin.rootcoverage.util.SimpleTemplate
import org.neotech.plugin.rootcoverage.util.SystemOutputWriter
import org.neotech.plugin.rootcoverage.util.createGradlePropertiesFile
import org.neotech.plugin.rootcoverage.util.createLocalPropertiesFile
import org.neotech.plugin.rootcoverage.util.getProperties
import org.neotech.plugin.rootcoverage.util.put
import org.neotech.plugin.rootcoverage.util.toGroovyString
import java.io.File
import java.util.Properties

@RunWith(Parameterized::class)
class IntegrationTest(
    // Used by Junit as the test name, see @Parameters
    @Suppress("unused") private val name: String,
    private val projectRoot: File,
    configurationFile: File,
    private val gradleVersion: String,
) {

    private val configuration = configurationFile.getProperties()

    @Before
    fun before(){
        // Ignore tests that require Gradle Managed Devices on CI (because GitHub Actions does not seem to support these well).
        val isGradleManagedDeviceTest = configuration.getProperty("runOnGradleManagedDevices", "false").toBoolean()
        Assume.assumeFalse(System.getenv("GITHUB_ACTIONS") != null && isGradleManagedDeviceTest)
    }

    @Test
    fun execute() {
        val template = SimpleTemplate().apply {
            putValue("configuration", configuration.toGroovyString())
        }

        File(projectRoot, "build.gradle.tmp").inputStream().use {
            File(projectRoot, "build.gradle").writeText(template.process(it, Charsets.UTF_8))
        }

        createLocalPropertiesFile(projectRoot)
        createGradlePropertiesFile(projectRoot, properties = Properties().apply {
            put("android.useAndroidX", "true")
            put(Properties().apply {
                val resource = GradleRunner::class.java.classLoader.getResourceAsStream("gradle.properties")
                load(resource)
            })
        })

        val runner = GradleRunner.create()
            .withProjectDir(projectRoot)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .forwardStdOutput(SystemOutputWriter.out())
            .forwardStdError(SystemOutputWriter.err())

            // Note: rootCodeCoverageReport is the old and deprecated name of the rootCoverageReport task, it is
            // used to check whether the old name properly aliases to the new task name.
            .withArguments("clean", "coverageReport", "rootCodeCoverageReport", "--stacktrace")

        val result = runner.build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        // Assert whether the combined coverage report is what we expected
        result.assertRootCoverageReport(File(projectRoot, "build/reports/jacoco.csv"))

        // Assert whether the per module coverage reports are what we expect
        result.assertAppCoverageReport()
        result.assertAndroidLibraryCoverageReport()

        assertSourceFilesHaveBeenAddedToReport(File(projectRoot, "build/reports/jacoco"))
    }

    private fun BuildResult.assertRootCoverageReport(file: File) {
        assertThat(task(":rootCoverageReport")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)

        // Also check if the old task name is still exe
        assertThat(task(":rootCodeCoverageReport")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val report = CoverageReport.from(file)

        report.assertCoverage("org.neotech.library.android", "LibraryAndroidJava")
        report.assertCoverage("org.neotech.library.android", "LibraryAndroidKotlin")
        report.assertCoverage("org.neotech.app", "AppJava")
        report.assertCoverage("org.neotech.app", "AppKotlin")
        report.assertCoverage("org.neotech.app", "RobolectricTestedActivity")
    }

    private fun BuildResult.assertAppCoverageReport() {
        assertThat(task(":app:coverageReport")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)

        if (configuration.getProperty("runOnGradleManagedDevices", "false").toBoolean()) {
            // Assert that the tests have been run on Gradle Managed Devices
            val device = configuration.getProperty("gradleManagedDeviceName", "allDevices")
            assertThat(task(":app:${device}DebugAndroidTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
            assertThat(task(":library_android:${device}DebugAndroidTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        } else {
            // Assert that the tests have been run on connected devices
            assertThat(task(":app:connectedDebugAndroidTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
            assertThat(task(":library_android:connectedDebugAndroidTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        val report = CoverageReport.from(File(projectRoot, "app/build/reports/jacoco.csv"))

        report.assertNotInReport("org.neotech.app", "MustBeExcluded")
        report.assertCoverage("org.neotech.app", "AppJava")
        report.assertCoverage("org.neotech.app", "AppKotlin")
        report.assertCoverage("org.neotech.app", "RobolectricTestedActivity")
    }

    private fun BuildResult.assertAndroidLibraryCoverageReport() {
        assertThat(task(":library_android:coverageReport")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val report = CoverageReport.from(File(projectRoot, "library_android/build/reports/jacoco.csv"))

        // Some coverage will be missing since the library also contains code that is only touched by tests in the app
        // module, which will not be touched when generating a module specific report.
        report.assertCoverage(
            packageName = "org.neotech.library.android",
            className = "LibraryAndroidJava",
            missedBranches = 0,
            missedInstructions = 8
        )
        report.assertCoverage(
            packageName = "org.neotech.library.android",
            className = "LibraryAndroidKotlin",
            missedBranches = 0,
            missedInstructions = 8
        )
    }

    private fun assertSourceFilesHaveBeenAddedToReport(htmlReportRoot: File) {
        htmlReportRoot.walkTopDown()
            .filter {
                it.isFile && it.extension == "html"
            }
            .forEach { file ->
                file.forEachLine {
                    assertThat(it).doesNotContainMatch("Source file &quot;.*&quot; was not found during generation of report\\.")
                }
            }
    }

    companion object {

        @Suppress("unused") // This method is used by the JVM (Parameterized JUnit Runner)
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): List<Array<Any>> {

            val testFixtures =
                File("src/test/test-fixtures").listFiles()?.filter { it.isDirectory } ?: error("Could not list test fixture directories")
            val gradleVersions = arrayOf("8.4")
            return testFixtures.flatMap { fixture ->
                val configurations = File(fixture, "configurations").listFiles() ?: error("Configurations folder not found in $fixture")
                configurations.flatMap { configuration ->
                    gradleVersions.map { gradleVersion ->
                        arrayOf(
                            "${fixture.name}-${configuration.nameWithoutExtension}-$gradleVersion",
                            fixture,
                            configuration,
                            gradleVersion
                        )
                    }
                }
            }
        }
    }
}
