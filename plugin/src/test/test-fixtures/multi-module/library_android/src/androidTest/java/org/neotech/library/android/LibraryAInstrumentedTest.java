package org.neotech.library.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LibraryAInstrumentedTest {

    @Test
    public void isJavaInstanceNotNull() {
        assertNotNull(LibraryAndroidJava.getInstance());
    }

    @Test
    public void isKotlinGetNameCorrect() {
        assertEquals("LibraryAndroidKotlin", LibraryAndroidKotlin.getName());
    }

    @Test
    public void isJavaGetNameCorrect() {
        assertEquals("LibraryAndroidJava", LibraryAndroidJava.getInstance().getName());
    }
}
