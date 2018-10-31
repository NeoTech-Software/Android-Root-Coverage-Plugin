package org.neotech.library.a;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LibraryAInstrumentedTest {

    @Test
    public void isJavaInstanceNotNull() {
        assertNotNull(LibraryAJava.getInstance());
    }

    @Test
    public void isKotlinGetNameCorrect() {
        assertEquals("LibraryAKotlin", LibraryAKotlin.getName());
    }

    @Test
    public void isJavaGetNameCorrect() {
        assertEquals("LibraryAJava", LibraryAJava.getInstance().getName());
    }
}
