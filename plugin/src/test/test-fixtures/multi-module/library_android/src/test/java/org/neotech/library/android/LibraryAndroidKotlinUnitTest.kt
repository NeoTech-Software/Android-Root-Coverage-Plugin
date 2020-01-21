package org.neotech.library.android

import org.junit.Assert
import org.junit.Test

class LibraryAndroidKotlinUnitTest {

    @Test
    fun touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByKotlinUnitTestInAndroidLibrary()
    }

    @Test
    fun touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByKotlinUnitTestInAndroidLibrary()
    }

    @Test
    fun touchGetInstanceToEnsureFullCoverage() {
        Assert.assertNotNull(LibraryAndroidJava.getInstance())
    }


}