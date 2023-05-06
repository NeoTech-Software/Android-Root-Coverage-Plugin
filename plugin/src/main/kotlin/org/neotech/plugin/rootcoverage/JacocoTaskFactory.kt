package org.neotech.plugin.rootcoverage

import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.neotech.plugin.rootcoverage.utilities.getReportOutputFile

private const val JACOCO_PLUGIN_NAME = "jacoco"

internal fun Project.createJacocoReportTask(
    taskName: String,
    taskGroup: String,
    taskDescription: String,
    rootProjectExtension: RootCoveragePluginExtension
): JacocoReport {
    val task = tasks.create(taskName, JacocoReport::class.java)

    // Make sure to only read from the rootProjectExtension after the project has been evaluated
    afterEvaluate {
        task.reports.apply {
            html.required.set(rootProjectExtension.generateHtml)
            xml.required.set(rootProjectExtension.generateXml)
            csv.required.set(rootProjectExtension.generateCsv)
        }
    }

    // Make sure to configure this JacocoReport task after the JaCoCoPlugin itself has been fully applied,
    // otherwise the JaCoCoPlugin may override settings in configureJacocoReportsDefaults()
    // https://github.com/gradle/gradle/blob/c177053ff95a1582c7919befe67993e0f1677f53/subprojects/jacoco/src/main/java/org/gradle/testing/jacoco/plugins/JacocoPlugin.java#L211
    pluginManager.withPlugin(JACOCO_PLUGIN_NAME) {
        task.group = taskGroup
        task.description = taskDescription
        task.reports.apply {
            html.outputLocation.set(getReportOutputFile("jacoco"))
            xml.outputLocation.set(getReportOutputFile("jacoco.xml"))
            csv.outputLocation.set(getReportOutputFile("jacoco.csv"))
        }
    }

    return task
}
