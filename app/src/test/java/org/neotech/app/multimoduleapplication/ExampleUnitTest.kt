package org.neotech.app.multimoduleapplication

import org.junit.Test

import org.junit.Assert.*
import org.neotech.library.a.LibraryAKotlin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun isGetNameCorrect() {
        assertEquals("LibraryAKotlin", LibraryAKotlin.getName())
    }
}
