package org.neotech.plugin.rootcoverage.util

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

fun BuildResult.assertSuccessful() {
    assertThat(output).contains("BUILD SUCCESSFUL")
}

fun BuildResult.assertTaskSuccess(taskPath: String) {
    assertThat(task(taskPath)!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
}

fun BuildResult.assertTaskNotExecuted(taskPath: String) {
    assertThat(task(taskPath)).isNull()
}