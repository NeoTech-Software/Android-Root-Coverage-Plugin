package org.neotech.library.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryAndroidUnitTest {

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