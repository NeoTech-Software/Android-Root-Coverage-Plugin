package org.neotech.plugin.rootcoverage

import org.gradle.api.file.FileCollection
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.util.GradleVersion

open class JacocoReportCompat : JacocoReport() {

    private var classDirectoriesCompat: FileCollection = project.files()
    private var sourceDirectoriesCompat: FileCollection = project.files()

    fun classDirectoriesFromCompat(fileCollection: FileCollection) {
        if (GradleVersion.current() >= GradleVersion.version("5.0.0")) {
            classDirectories.from(fileCollection)
        } else {
            classDirectoriesCompat = classDirectoriesCompat.plus(fileCollection)
            setClassDirectories(classDirectoriesCompat)
        }
    }

    fun sourceDirectoriesFromCompat(fileCollection: FileCollection) {
        if (GradleVersion.current() >= GradleVersion.version("5.0.0")) {
            sourceDirectories.from(fileCollection)
        } else {
            sourceDirectoriesCompat = sourceDirectoriesCompat.plus(fileCollection)
            setSourceDirectories(sourceDirectoriesCompat)
        }
    }

    fun executionDataFromCompat(fileCollection: FileCollection) {
        if (GradleVersion.current() >= GradleVersion.version("5.0.0")) {
            executionData.from(fileCollection)
        } else {
            executionData(fileCollection)
        }
    }
}