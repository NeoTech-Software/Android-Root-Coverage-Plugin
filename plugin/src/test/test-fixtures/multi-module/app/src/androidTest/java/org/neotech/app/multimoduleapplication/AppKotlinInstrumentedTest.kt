package org.neotech.app.multimoduleapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.neotech.app.AppJava
import org.neotech.app.AppKotlin
import org.neotech.library.android.LibraryAndroidJava
import org.neotech.library.android.LibraryAndroidKotlin

@RunWith(AndroidJUnit4::class)
class AppKotlinInstrumentedTest {

    @Test
    fun touchJavaCodeInApp() {
        AppJava.getInstance().touchedByKotlinInstrumentedTestInApp()
    }

    @Test
    fun touchKotlinCodeInApp() {
        AppKotlin.touchedByKotlinInstrumentedTestInApp()
    }

    @Test
    fun touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByKotlinInstrumentedTestInApp()
    }

    @Test
    fun touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByKotlinInstrumentedTestInApp()
    }
}