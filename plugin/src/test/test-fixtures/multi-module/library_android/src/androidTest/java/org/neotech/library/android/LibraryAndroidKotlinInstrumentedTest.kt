package org.neotech.library.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryAndroidKotlinInstrumentedTest {

    @Test
    fun touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByKotlinInstrumentedTestInAndroidLibrary()
    }

    @Test
    fun touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByKotlinInstrumentedTestInAndroidLibrary()
    }

    @Test
    fun touchGetInstanceToEnsureFullCoverage() {
        Assert.assertNotNull(LibraryAndroidJava.getInstance())
    }
}