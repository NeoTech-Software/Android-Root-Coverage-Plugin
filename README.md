[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/nl.neotech.plugin/android-root-coverage-plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/nl.neotech.plugin.rootcoverage)
[![Maven Central](https://img.shields.io/maven-central/v/nl.neotech.plugin/android-root-coverage-plugin?label=Maven%20Central)](https://search.maven.org/artifact/nl.neotech.plugin/android-root-coverage-plugin)
[![Build](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/actions/workflows/build.yml)
[![Coverage](https://img.shields.io/codecov/c/github/NeoTech-Software/Android-Root-Coverage-Plugin/master)](https://app.codecov.io/gh/NeoTech-Software/Android-Root-Coverage-Plugin/branch/master)

# Android-Root-Coverage-Plugin
**Automatically configures Jacoco code coverage tasks for both combined and per module coverage reports.**

Configuring Jacoco for Android projects is unfortunately not always easy. It is very common to find multi-module Android
projects where one module has tests for code found in another module, for example integration/UI tests that cover code
in multiple other modules. Configuring Jacoco for such a case is not always straight forward, you need to point Jacoco
to the right sources, execution data and class files, not to mention how error prone manual Jacoco setups can be. This
plugin automatically configures Jacoco for you, so you don't have to.

**Feature highlights:**
- Include unit-tests, instrumented unit-tests or both in the final reports
- Support for combined coverage reports *(code in module X is covered when touched by tests from any other module)*
- Support for coverage reports per module *(code in module X is only covered when touched by tests from module X)*
- Custom package/class filters
- Support for mixed build-types

# 1. Setup
Apply the plugin to your top-level (root project) `build.gradle` file using one of the
following methods:

<details open>
  <summary><strong>Plugin block (Kotlin):</strong></summary>

  ```kotlin
  plugins {
      // Add the plugin to the plugin block
      id("nl.neotech.plugin.rootcoverage") version "1.11.0"
  }
  ```
</details>

<details>
  <summary><strong>Plugin block (Groovy):</strong></summary>

  ```groovy
  plugins {
   // Add the plugin to the plugin block
   id "nl.neotech.plugin.rootcoverage" version "1.11.0"
  }
  ```
</details>

<blockquote>

<details>
  <summary><strong>Still using classpath & apply?</strong></summary>

   **Groovy**
   ```groovy
   apply plugin: 'nl.neotech.plugin.rootcoverage'
   
   buildscript {
      dependencies {
         classpath 'nl.neotech.plugin:android-root-coverage-plugin:1.11.0'
      }
   }
   ```
</details>
</blockquote>

# 2. How to use

1. Enable running tests with code coverage
   This is required so that code will be instrumented, but also to tell this plugin to include
   modules that have at least one of these properties enabled in the final report (or individual
   reports)

<details open>
  <summary><strong>Kotlin</strong> (.gradle.kts)</summary>

   ```kotlin
   android {
       buildTypes {
           debug {
               // AGP 7.3+ (at least one should be true for this module to be included in the reporting)
               enableUnitTestCoverage = true
               enableAndroidTestCoverage = true
   
               // AGP before 7.3
               testCoverageEnabled = true
           }
       }
   }
   ```
</details>

<details>
  <summary><strong>Groovy</strong> (.gradle)</summary>

   ```groovy
   android {
       buildTypes {
           debug {
               // AGP 7.3+ (at least one should be true for this module to be included in the reporting)
               enableUnitTestCoverage true
               enableAndroidTestCoverage true
   
               // AGP before 7.3
               testCoverageEnabled true
           }
       }
   }
   ```
</details>

   > Only Android modules (`com.android.application` or `com.android.library`) are supported, this plugin will not execute
   tests and generate coverage reports for non-android modules. Also keep in mind that by default
   this plugin is configured to create reports for the `debug` variant, so coverage must be
   enabled for the `debug` variant, unless you change this ([Configuration](#3-configuration)).

2. Run one of the automatically configured Gradle tasks to generate a Jacoco report:
   - **For combined coverage:** `./gradlew :rootCoverageReport`
   - **For module specific coverage (all modules):** `./gradlew coverageReport`
   - **For module specific coverage (single module):** `./gradlew :yourModule:coverageReport`

   > Resulting reports can be found in `/build/reports/` and `yourModule/build/reports/`

3. Optionally configure the plugin to change the output types, test variants and more, see
   [Configuration](#3-configuration).


# 3. Configuration
By default the plugin generates code coverage reports using the build variant `debug` for every
module. However in some cases different build variants per module might be required, especially if
there is no `debug` build variant available. In those cases you can configure custom build variants
for specific modules:

>**Place this in the project root build.gradle file!**

<details open>
  <summary><strong>Kotlin</strong> (.gradle.kts)</summary>

   ```kotlin
   rootCoverage {
      // The default build variant for every module
      buildVariant = "debug"
      // Overrides the default build variant for specific modules.
      buildVariantOverrides = mapOf(":moduleA" to "debugFlavourA", ":moduleB" to "debugFlavourA")
   
      // Class & package exclude patterns
      excludes = ["**/some.package/**"]
   
      // Since 1.1 generateHtml is by default true
      generateCsv = false
      generateHtml = true
      generateXml = false
   
      // Since 1.2: When true this plugin will run the necessary Gradle tasks to execute instrumented Android tests
      executeAndroidTests = true
   
      // Since 1.2: When true this plugin will run the necessary Gradle tasks to execute unit tests
      executeUnitTests = true
   
      // Since 1.2: When true include results from instrumented Android tests into the coverage report
      includeAndroidTestResults = true
   
      // Since 1.2: When true include results from unit tests into the coverage report
      includeUnitTestResults = true
      
      // Since 1.4: Sets jacoco.includeNoLocationClasses, so you don't have to. Helpful when using Robolectric
      // which usually requires this attribute to be true
      includeNoLocationClasses = false
   
      // Since 1.7 (experimental): If set to true instrumented tests will be attempt to run on
      // Gradle Managed Devices before trying devices connected through other means (ADB).
      runOnGradleManagedDevices = false
      
      // Since 1.7 (experimental): The name of the Gradle Managed device to run instrumented tests on.
      // This is only used if `runOnGradleManagedDevices` is set to true. If not given tests will be
      // run on all available Gradle Managed Devices
      gradleManagedDeviceName = "smallphoneapi32"
   }
   ```
</details>

<details>
  <summary><strong>Groovy</strong> (.gradle)</summary>

   ```groovy
   rootCoverage {
      // The default build variant for every module
      buildVariant "debug"
      // Overrides the default build variant for specific modules.
      buildVariantOverrides ":moduleA" : "debugFlavourA", ":moduleB": "debugFlavourA"
   
      // Class & package exclude patterns
      excludes = ["**/some.package/**"]
   
      // Since 1.1 generateHtml is by default true
      generateCsv false
      generateHtml true
      generateXml false

      // Since 1.2: When true this plugin will run the necessary Gradle tasks to execute instrumented Android tests
      executeAndroidTests true

      // Since 1.2: When true this plugin will run the necessary Gradle tasks to execute unit tests
      executeUnitTests true
   
      // Since 1.2: When true include results from instrumented Android tests into the coverage report
      includeAndroidTestResults true
   
      // Since 1.2: When true include results from unit tests into the coverage report
      includeUnitTestResults true
      
      // Since 1.4: Sets jacoco.includeNoLocationClasses, so you don't have to. Helpful when using Robolectric
      // which usually requires this attribute to be true
      includeNoLocationClasses false
   
      // Since 1.7 (experimental): If set to true instrumented tests will be attempt to run on
      // Gradle Managed Devices before trying devices connected through other means (ADB).
      runOnGradleManagedDevices false
      
      // Since 1.7 (experimental): The name of the Gradle Managed device to run instrumented tests on.
      // This is only used if `runOnGradleManagedDevices` is set to true. If not given tests will be
      // run on all available Gradle Managed Devices
      gradleManagedDeviceName "smallphoneapi32"
   }
   ```
</details>

# 4. Compatibility
| Version             | [Android Gradle plugin version](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle) | Gradle version         |
|---------------------|--------------------------------------------------------------------------------------------------------------|------------------------|
| **1.12.0-SNAPSHOT** | 9.0+                                                                                                         | 9.1+                   |
| **1.11.0**          | 8.11+                                                                                                        | 8.13+                  |
| **1.10.0**          | 8.8+                                                                                                         | 8.10.2+                |
| **1.9.0**           | 8.6+                                                                                                         | 8.7+                   |
| **1.8.0**           | 8.5.2<br/>8.4.2<br/>8.3.0-alpha05 - 8.3.2                                                                    | 8.6+<br/>8.5+<br/>8.4+ |
| **Note 1**          | 8.0 - 8.3.0-alpha04                                                                                          | n.a.                   |
| **1.7.1**           | 7.4                                                                                                          | 7.5+                   |
| **1.6.0**           | 7.3                                                                                                          | 7.4+                   |
| **1.5.3**           | 7.2                                                                                                          | 7.3+                   |
| **Note 2**          | 7.0 - 7.2.0-alpha05                                                                                          | n.a.                   |
| **1.4.0**           | 4.2<br/>4.1                                                                                                  | 6.7.1+<br/>6.5+        |
| **1.3.1**           | 4.0<br/>3.6                                                                                                  | 6.1.1+<br/>5.6.4+      |
| **1.2.1**           | 3.5                                                                                                          | 5.4.1+                 |
| **1.1.2**           | 3.4                                                                                                          | 5.1.1+                 |
| **1.1.1**           | 3.3                                                                                                          | 4.10.1+                |
| **1.0.2**           | 3.2                                                                                                          | 4.6+                   |

<details>
  <summary><b>Note 1: AGP 8.0-8.3.0-alpha04</b></summary>
  
  *Android Gradle Plugin version 8.0 till 8.3.0-alpha04 suffered from a [bug](https://issuetracker.google.com/u/0/issues/281266702) that made it impossible (by normal means) to get access to non-instrumented class files, this bug lasted for a long time and was only fixed in 8.3.0-alpha05. This means there is no stable working plugin version available for these AGP versions.*
</details>

<details>
  <summary><b>Note 2: AGP 7.0-7.2.0-alpha05</b></summary>
  
  *Android Gradle Plugin version 7 till 7.2.0-alpha05 suffered from a [bug](https://issuetracker.google.com/issues/195860510) that caused instrumented coverage in Android library modules to fail, this has only been [fixed](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/issues/36#issuecomment-977241070) in Android Gradle Plugin 7.2.0-alpha06. This means there is no stable working plugin version available for these AGP versions.*
</details>

<details>
  <summary><b>Note 3: Versions 1.0.2 to 1.3.0</b></summary>
  
  *Plugin versions below 1.3.1, such as 1.3.0, are only available on the Gradle Plugin Portal and not on Maven Central. These versions use the group ID `org.neotech.plugin` and plugin ID `org.neotech.plugin.rootcoverage`! For more info see: [Bintray/JCenter shutdown](#4-bintrayjcenter-shutdown).*
</details>

<details>
  <summary><b>Note 4: AGP versions before 3.4.0-alpha05</b></summary>

  *Android Gradle Plugin versions before `3.4.0-alpha05` are affected by a bug that in certain conditions can cause Jacoco instrumentation to fail in combination with inline kotlin methods shared across modules. For more information see: [issue #109771903](https://issuetracker.google.com/issues/109771903) and [issue #110763361](https://issuetracker.google.com/issues/110763361). If your project is affected by this upgrade to an Android Gradle Plugin version of at least `3.4.0-alpha05`.*
</details>


# 4. Bintray/JCenter shutdown
Due to the [shutdown of Bintray/JCenter](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)
the Android-Root-Coverage-Plugin has been migrated to Sonatype's Maven Central repository. Unfortunately this also
meant that the group ID used by the Android-Root-Coverage-Plugin had to be changed from `org.neotech.plugin` to
`nl.neotech.plugin`. The plugin ID has also changed from `org.neotech.plugin.rootcoverage` to `nl.neotech.plugin.rootcoverage`.

<details>
  <summary><strong>More info</strong></summary>

   JCenter is supposed to stay available as read-only repository, however it is probably better to
   migrate to the Gradle Plugin Portal, as it is the official Gradle repository for plugins, and all
   versions of this plugin are available there:
   ```groovy
   pluginManagement {
      repositories {
         // Add this repository to your build script for plugin resolution, above mavenCentral() or jcenter()
         gradlePluginPortal()
      }
   }
   ```
   Version 1.3.0 has been re-released (as 1.3.1) with the new group ID and plugin ID to Maven Central and the
   Gradle Plugin Portal. Upcoming versions will also be released to Maven Central and the Gradle Plugin Portal.
   Check the [setup](#1-setup) section on how to use this plugin with the updated group ID and plugin ID.

</details>


# 5. Development
Want to contribute? Great! Just clone the repo, code away and create a pull-request. Try to keep changes small and make
sure to follow the code-style as found in the rest of the project.

**How to test your changes/additions?**
The plugin comes with an integration test and unit tests. You can run these tests either by executing
`./gradlew clean test` or run the test directly from Android Studio (or IntelliJ IDEA).
