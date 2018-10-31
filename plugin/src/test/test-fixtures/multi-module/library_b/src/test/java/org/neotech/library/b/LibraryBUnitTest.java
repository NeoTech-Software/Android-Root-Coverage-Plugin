package org.neotech.library.b;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LibraryBUnitTest {

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