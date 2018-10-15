# Android-Root-Coverage-Plugin
**A Gradle plugin for easy generation of combined code coverage reports for Android projects with multiple modules.**
Generating code coverage reports for Android Projects that make use of the Gradle build is quite easy. Unfortunately by default code coverage is generated separately per module, this means each modules takes into account it's own sources and tests, which is in terms of domain separation fine. However it is very common to find multi-module Android project where only one module has instrumented tests, or full-fledged UI tests using Espresso. This plugin comes in handy for those projects. It generates code coverage reports using Jacoco taking into account all the modules and tests at once.

  - Supports both Android app and library modules (`com.android.application` & `com.android.library`).
  - Supports different build variants per module within the same report.
  - Supports custom filters.

# Setup
Apply the Android-Root-Coverage-Plugin plugin to your top-level (root project) gradle file following these 3 steps:

```
// Step 3: Apply the plugin to the top-level gradle file
apply plugin: 'org.neotech.plugin.rootcoverage'

buildscript {
    repositories {
        // Step 1: add the repository
        // This plugin is not yet available on Maven Central, so currently it is needed to add the following repository:
        maven {
            url  "http://dl.bintray.com/rolf-smit/maven"
        }
    }
    dependencies {
        // Step 2: add the dependency
        classpath 'org.neotech.plugin:android-root-coverage-plugin:0.0.1-dev'
    }
}
```

# How to use
Currently only modules with the plugin type `com.android.application` or `com.android.library` are taken into account when generating the root code coverage report, besides this any module that does not have `testCoverageEnabled true` for the default build variant (`debug`) will be skipped::

You can add a module by enabling `testCoverageEnabled`:
```
android {
    buildTypes {
        debug {
            testCoverageEnabled true
        }
    }
}
```

The Android-Root-Coverage-Plugin generates a special Gradle task `:rootCodeCoverageReport` that when executed generates a Jacoco code coverage report. You can either run this task directly from Android Studio using the Gradle Tool Window (see: https://www.jetbrains.com/help/idea/jetgradle-tool-window.html) or from the terminal.

- **Gradle Tool Window:** You can find the task under: `Tasks > reporting > rootCodeCoverageReport`, double click to  execute it.
- **Terminal:** Execute the task using `gradlew rootCodeCoverageReport`.

# Configuration
By default the plugin generates code coverage reports using the build variant `debug` for every module. However in some cases different build variants per module might be required, especially if there is no `debug` build variant available. In those cases you can configure custom build variants for specific modules:

```
rootCoverage {
    // The default build variant for every module
    buildVariant "debug"
    // Overrides the default build variant for specific modules.
    buildVariantOverrides ":moduleA" : "debugFlavourA", ":moduleB": "debugFlavourA"
    // Class exclude patterns
    excludes ["**/library.a/**"]
}
```


# Development
Want to contribute? Great! Currently this plugin is in need of extensive testing. Besides this there is also a small wish list:

- Support for Java modules
- Make use of the JacocoMerge task? To merge the `exec` en `ec` files?
- Support for configuring the output type: html, xml etc. (Just like Jacoco)
- Actual Plugin unit-tests (instead of a test project)

# Author note
Many thanks to [Hans van Dam](https://github.com/hansvdam) for helping with testing and the inital idea.