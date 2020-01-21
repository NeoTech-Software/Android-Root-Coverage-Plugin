package org.neotech.app

object AppKotlin {

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
