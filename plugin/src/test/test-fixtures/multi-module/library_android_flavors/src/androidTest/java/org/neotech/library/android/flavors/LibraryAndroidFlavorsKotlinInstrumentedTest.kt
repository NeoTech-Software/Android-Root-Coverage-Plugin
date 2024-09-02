package org.neotech.library.android.flavors

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryAndroidFlavorsKotlinInstrumentedTest {

    @Test
    fun touch() {
        LibraryAndroidFlavorsKotlin.touchedByInstrumentedTest()
    }
}