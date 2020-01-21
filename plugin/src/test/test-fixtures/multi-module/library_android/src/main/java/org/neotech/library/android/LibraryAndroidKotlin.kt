package org.neotech.library.android

object LibraryAndroidKotlin {

    @JvmStatic
    fun touchedByJavaInstrumentedTestInAndroidLibrary(): String {
        return "touchedByJavaInstrumentedTestInAndroidLibrary"
    }

    @JvmStatic
    fun touchedByJavaUnitTestInAndroidLibrary(): String {
        return "touchedByJavaUnitTestInAndroidLibrary"
    }

    fun touchedByKotlinInstrumentedTestInAndroidLibrary(): String {
        return "touchedByKotlinInstrumentedTestInAndroidLibrary"
    }

    fun touchedByKotlinUnitTestInAndroidLibrary(): String {
        return "touchedByKotlinUnitTestInAndroidLibrary"
    }

    @JvmStatic
    fun touchedByJavaInstrumentedTestInApp(): String {
        return "touchedByJavaInstrumentedTestInApp"
    }

    @JvmStatic
    fun touchedByJavaUnitTestInApp(): String {
        return "touchedByJavaUnitTestInApp"
    }

    fun touchedByKotlinInstrumentedTestInApp(): String {
        return "touchedByKotlinInstrumentedTestInApp"
    }
    
    fun touchedByKotlinUnitTestInApp(): String {
        return "touchedByKotlinUnitTestInApp"
    }
}
