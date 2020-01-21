package org.neotech.app.multimoduleapplication

import org.junit.Test
import org.neotech.app.AppJava
import org.neotech.app.AppKotlin
import org.neotech.library.android.LibraryAndroidJava
import org.neotech.library.android.LibraryAndroidKotlin

class AppKotlinUnitTest {

    @Test
    fun touchJavaCodeInApp() {
        AppJava.getInstance().touchedByKotlinUnitTestInApp()
    }

    @Test
    fun touchKotlinCodeInApp() {
        AppKotlin.touchedByKotlinUnitTestInApp()
    }

    @Test
    fun touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByKotlinUnitTestInApp()
    }

    @Test
    fun touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByKotlinUnitTestInApp()
    }
}