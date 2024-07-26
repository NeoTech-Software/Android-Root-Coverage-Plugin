package org.neotech.plugin.rootcoverage

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.neotech.plugin.rootcoverage.util.SimpleTemplate
import org.neotech.plugin.rootcoverage.util.SystemOutputWriter
import org.neotech.plugin.rootcoverage.util.assertSuccessful
import org.neotech.plugin.rootcoverage.util.assertTaskSuccess
import org.neotech.plugin.rootcoverage.util.createGradlePropertiesFile
import org.neotech.plugin.rootcoverage.util.createLocalPropertiesFile
import org.neotech.plugin.rootcoverage.util.put
import org.neotech.plugin.rootcoverage.util.readYaml
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

    private val configuration: TestConfiguration = configurationFile.readYaml()

    @Before
    fun before() {
        Assume.assumeFalse(configuration.ignore)
    }

    @Test
    fun execute() {

        val templateRootBuildGradleFile = SimpleTemplate().apply {
            putValue("configuration", configuration.pluginConfiguration.properties.toGroovyString())
        }
        File(projectRoot, "build.gradle.tmp").inputStream().use {
            File(projectRoot, "build.gradle").writeText(templateRootBuildGradleFile.process(it, Charsets.UTF_8))
        }

        val templateAppBuildGradleFile = SimpleTemplate().apply {
            putValue(
                "managedDevices", if (configuration.projectConfiguration.addGradleManagedDevice) {
                    """
                    managedDevices {
                        devices {
                            nexusoneapi30 (com.android.build.api.dsl.ManagedVirtualDevice) {
                                device = "Nexus One"
                                apiLevel = 30
                                systemImageSource = "aosp-atd"
                            }
                        }
                    }
                    """.trimIndent()
                } else {
                    ""
                }
            )

            putValue("defaultConfig.clearPackageData", "testInstrumentationRunnerArguments clearPackageData: 'true'".takeIf { configuration.projectConfiguration.clearPackageData })

            val testOrchestrator = configuration.projectConfiguration.testOrchestrator

            putValue("defaultConfig.testOrchestrator", "testInstrumentationRunnerArguments useTestStorageService: 'true'".takeIf { testOrchestrator })
            putValue("testOptions.testOrchestrator", "execution 'ANDROIDX_TEST_ORCHESTRATOR'".takeIf { testOrchestrator })
            putValue("dependencies.testOrchestrator", "androidTestUtil libs.testOrchestrator".takeIf { testOrchestrator })
        }
        File(projectRoot, "app/build.gradle.tmp").inputStream().use {
            File(projectRoot, "app/build.gradle").writeText(templateAppBuildGradleFile.process(it, Charsets.UTF_8))
        }

        createLocalPropertiesFile(projectRoot)
        createGradlePropertiesFile(projectRoot, properties = Properties().apply {
            put("android.useAndroidX", "true")
            put(Properties().apply {
                val resource = GradleRunner::class.java.classLoader.getResourceAsStream("gradle.properties")
                load(resource)
            })
        })

        // Note: rootCodeCoverageReport is the old and deprecated name of the rootCoverageReport task, it is
        // used to check whether the old name properly aliases to the new task name.
        val gradleCommands = if (configuration.pluginConfiguration.getPropertyValue("executeAndroidTests") == "false") {
            val runOnGradleManagedDevices = configuration.pluginConfiguration.getPropertyValue("runOnGradleManagedDevices") ?: "false"
            if (runOnGradleManagedDevices == "false") {
                listOf("clean", "connectedDebugAndroidTest", "coverageReport", "rootCodeCoverageReport", "--stacktrace")
            } else {
                listOf("clean", "nexusoneapi30DebugAndroidTest", "coverageReport", "rootCodeCoverageReport", "--stacktrace")
            }
        } else {
            listOf("clean", "coverageReport", "rootCodeCoverageReport", "--stacktrace")
        }

        val runner = GradleRunner.create()
            .withProjectDir(projectRoot)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .forwardStdOutput(SystemOutputWriter.out())
            .forwardStdError(SystemOutputWriter.err())
            .withArguments(gradleCommands)

        val result = runner.build()

        result.assertSuccessful()

        // Assert whether the correct Android Test tasks are executed
        result.assertCorrectAndroidTestTasksAreExecuted()

        // Assert whether the combined coverage report is what we expected
        result.assertRootCoverageReport()

        // Assert whether the per module coverage reports are what we expect
        result.assertAppCoverageReport()
        result.assertAndroidLibraryCoverageReport()

        assertSourceFilesHaveBeenAddedToReport(File(projectRoot, "build/reports/jacoco"))
    }

    private fun BuildResult.assertCorrectAndroidTestTasksAreExecuted() {
        if (configuration.pluginConfiguration.getPropertyValue("runOnGradleManagedDevices", "false").toBoolean()) {
            // Assert that the tests have been run on Gradle Managed Devices
            val device = configuration.pluginConfiguration.getPropertyValue("gradleManagedDeviceName", "allDevices")
            assertTaskSuccess(":app:${device}DebugAndroidTest")
            assertTaskSuccess(":library_android:${device}DebugAndroidTest")

        } else {
            // Assert that the tests have been run on connected devices
            assertTaskSuccess(":app:connectedDebugAndroidTest")
            assertTaskSuccess(":library_android:connectedDebugAndroidTest")
        }
    }

    private fun BuildResult.assertRootCoverageReport() {
        assertTaskSuccess(":rootCoverageReport")

        // Also check if the old task name is still executed
        assertTaskSuccess(":rootCodeCoverageReport")

        val report = CoverageReport.from(File(projectRoot, "build/reports/jacoco.csv"))

        report.assertCoverage("org.neotech.library.android", "LibraryAndroidJava")
        report.assertCoverage("org.neotech.library.android", "LibraryAndroidKotlin")
        report.assertCoverage("org.neotech.app", "AppJava")
        report.assertCoverage("org.neotech.app", "AppKotlin")
        report.assertCoverage("org.neotech.app", "RobolectricTestedActivity")
    }

    private fun BuildResult.assertAppCoverageReport() {
        assertTaskSuccess(":app:coverageReport")
        val report = CoverageReport.from(File(projectRoot, "app/build/reports/jacoco.csv"))

        report.assertNotInReport("org.neotech.app", "MustBeExcluded")
        report.assertCoverage("org.neotech.app", "AppJava")
        report.assertCoverage("org.neotech.app", "AppKotlin")
        report.assertCoverage("org.neotech.app", "RobolectricTestedActivity")
    }

    private fun BuildResult.assertAndroidLibraryCoverageReport() {
        assertTaskSuccess(":library_android:coverageReport")

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

            val fixture = File("src/test/test-fixtures/multi-module")

            val gradleVersions = arrayOf("8.4")

            val configurations = File(fixture, "configurations").listFiles() ?: error("Configurations folder not found in $fixture")
            return configurations.flatMap { configuration ->
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

    data class TestConfiguration(
        val ignore: Boolean = false,
        val projectConfiguration: ProjectConfiguration,
        val pluginConfiguration: PluginConfiguration
    ) {
        data class PluginConfiguration(val properties: List<Property> = emptyList()) {

            fun getPropertyValue(name: String, defaultValue: String): String = getPropertyValue(name) ?: defaultValue

            fun getPropertyValue(name: String): String? = properties.find { it.name == name }?.value


            data class Property(val name: String, val value: String)
        }

        data class ProjectConfiguration(
            val addGradleManagedDevice: Boolean = true,
            val clearPackageData: Boolean = false,
            val testOrchestrator: Boolean = false,
        )
    }
}
