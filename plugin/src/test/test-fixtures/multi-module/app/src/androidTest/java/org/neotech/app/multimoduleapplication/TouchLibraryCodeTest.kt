package org.neotech.app.multimoduleapplication

import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.neotech.library.android.LibraryAndroidJava
import org.neotech.library.android.LibraryAndroidKotlin

@RunWith(AndroidJUnit4::class)
class TouchLibraryCodeTest {

    @Test
    fun touchAndroidLibraryCodeJava() {
        LibraryAndroidJava.getInstance().getName2()
    }

    @Test
    fun touchAndroidLibraryCodeKotlin() {
        LibraryAndroidKotlin.getName2()
    }
}
