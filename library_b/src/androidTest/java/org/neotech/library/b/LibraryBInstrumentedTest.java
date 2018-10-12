package org.neotech.library.b;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LibraryBInstrumentedTest {

    @Test
    public void isJavaInstanceNotNull() {
        assertNotNull(LibraryBJava.getInstance());
    }

    @Test
    public void isKotlinGetNameCorrect() {
        assertEquals("LibraryBKotlin", LibraryBKotlin.getName());
    }

    @Test
    public void isJavaGetNameCorrect() {
        assertEquals("LibraryBJava", LibraryBJava.getInstance().getName());
    }
}
