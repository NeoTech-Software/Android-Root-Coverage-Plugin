package org.neotech.library.a;

import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryAUnitTest {

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