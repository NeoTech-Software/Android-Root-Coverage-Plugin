package org.neotech.app.multimoduleapplication

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
//import org.neotech.library.a.LibraryAKotlin

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("org.neotech.app.multimoduleapplication", appContext.packageName)
    }

    @Test
    fun isGetNameCorrect() {
        //assertEquals("LibraryAKotlin", LibraryAKotlin.getName())
    }
}
