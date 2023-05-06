[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/nl.neotech.plugin/android-root-coverage-plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/nl.neotech.plugin.rootcoverage)
[![Maven Central](https://img.shields.io/maven-central/v/nl.neotech.plugin/android-root-coverage-plugin?label=Maven%20Central)](https://search.maven.org/artifact/nl.neotech.plugin/android-root-coverage-plugin)
[![Build](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/actions/workflows/build.yml)
[![Coverage](https://img.shields.io/codecov/c/gh/NeoTech-Software/Android-Root-Coverage-Plugin/branch/master?token=4I3MXF2WLX)](https://app.codecov.io/gh/NeoTech-Software/Android-Root-Coverage-Plugin/branch/master)

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
  <summary><strong>Plugin block:</strong></summary>

  ```groovy
  // Below buildscript {}
  plugins {
      id "nl.neotech.plugin.rootcoverage" version "1.6.0"
  }
  ```
</details>

<details>
  <summary><strong>classpath + apply:</strong></summary>

   ```groovy
   apply plugin: 'nl.neotech.plugin.rootcoverage'
   
   buildscript {
      dependencies {
         classpath 'nl.neotech.plugin:android-root-coverage-plugin:1.6.0'
      }
   }
   ```
</details>


# 2. How to use

1. Enable running tests with code coverage
   This is required so that code will be instrumented, but also to tell this plugin to include
   modules that have at least one of these properties enabled in the final report (or individual
   reports)

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

   // Since 1.2: Same as executeTests except that this only affects the instrumented Android tests
   executeAndroidTests true

   // Since 1.2: Same as executeTests except that this only affects the unit tests
   executeUnitTests true

   // Since 1.2: When true include results from instrumented Android tests into the coverage report
   includeAndroidTestResults true

   // Since 1.2: When true include results from unit tests into the coverage report
   includeUnitTestResults true
   
   // Since 1.4: Sets jacoco.includeNoLocationClasses, so you don't have to. Helpful when using Robolectric
   // which usually requires this attribute to be true
   includeNoLocationClasses false

   // Upcoming in 1.7: If set to true instrumented tests will be attempt to run on
   // Gradle Managed Devices before trying devices connected through other means (ADB).
   runOnGradleManagedDevices false
   
   // Upcoming in 1.7: The name of the Gradle Managed device to run instrumented tests on.
   // This is only used if `runOnGradleManagedDevices` is set to true. If not given tests will be
   // run on all available Gradle Managed Devices
   gradleManagedDeviceName "nexusoneapi30"
}
```


# 4. Compatibility
| Version        | [Android Gradle plugin version](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle) | Gradle version    |
|----------------|--------------------------------------------------------------------------------------------------------------|-------------------|
| **1.6.0**      | 7.3                                                                                                          | 7.4+              |
| **1.5.3**      | 7.2                                                                                                          | 7.3+              |
| **See note 2** | 7.0-7.2-alpha05                                                                                              | n.a.              |
| **1.4.0**      | 4.2<br/>4.1                                                                                                  | 6.7.1+<br/>6.5+   |
| **1.3.1**      | 4.0<br/>3.6                                                                                                  | 6.1.1+<br/>5.6.4+ |
| **1.2.1**      | 3.5                                                                                                          | 5.4.1+            |
| **1.1.2**      | 3.4                                                                                                          | 5.1.1+            |
| **1.1.1**      | 3.3                                                                                                          | 4.10.1+           |
| **1.0.2**      | 3.2                                                                                                          | 4.6+              |

> *Note 1: Plugin versions below 1.3.1, such as 1.3.0, are only available on the Gradle Plugin Portal
(`maven { url "https://plugins.gradle.org/m2/"}`) and not on Maven Central. These versions use the
group ID `org.neotech.plugin` and plugin ID `org.neotech.plugin.rootcoverage`!*

> *Note 2: Android Gradle Plugin version 7 till 7.2.0-alpha05 suffered from a
[bug](https://issuetracker.google.com/issues/195860510) that caused instrumented coverage in Android library modules to fail, this has only been
[fixed](https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/issues/36#issuecomment-977241070)
in Android Gradle Plugin 7.2.0-alpha06. This means there is no stable working plugin version available for these AGP versions.*

> *Note 3: Android Gradle Plugin versions before `3.4.0-alpha05` are affected by a bug that in certain conditions can 
cause Jacoco instrumentation to fail in combination with inline kotlin methods shared across modules. For more information
see: <https://issuetracker.google.com/issues/109771903> and <https://issuetracker.google.com/issues/110763361>.
If your project is affected by this upgrade to an Android Gradle Plugin version of at least `3.4.0-alpha05`.*


# 4. Bintray/JCenter shutdown
Due to the [shutdown of Bintray/JCenter](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)
the Android-Root-Coverage-Plugin has been migrated to Sonatype's Maven Central repository. Unfortunately this also
meant that the group ID used by the Android-Root-Coverage-Plugin had to be changed from `org.neotech.plugin` to
`nl.neotech.plugin`. The plugin ID has also changed from `org.neotech.plugin.rootcoverage` to `nl.neotech.plugin.rootcoverage`.

 JCenter is supposed to stay available as read-only repository, however it is probably better to migrate to
 the Gradle Plugin Portal, since all version of this plugin are also available there:
```groovy
pluginManagement {
   repositories {
      gradlePluginPortal()
   }
}
```
Version 1.3.0 has been re-released (as 1.3.1) with the new group ID and plugin ID to Maven Central and the
Gradle Plugin Portal. Upcoming versions will also be released to Maven Central and the Gradle Plugin Portal.
Check the [setup](#1-setup) section on how to use this plugin with the updated group ID and plugin ID.


# 5. Development
Want to contribute? Great! Just clone the repo, code away and create a pull-request. Try to keep changes small and make
sure to follow the code-style as found in the rest of the project.

**How to test your changes/additions?**
The plugin comes with an integration test and unit tests. You can run these tests either by executing
`./gradlew clean test` or run the test directly from Android Studio (or IntelliJ IDEA).
