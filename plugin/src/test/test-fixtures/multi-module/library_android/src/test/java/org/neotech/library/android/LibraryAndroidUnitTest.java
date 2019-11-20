package org.neotech.library.android;

import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryAndroidUnitTest {

    @Test
    public void touchJavaCode() {
        LibraryAndroidJava.getInstance().touchedByUnitTest();
    }

    @Test
    public void touchKotlinCode() {
        LibraryAndroidKotlin.touchedByUnitTest();
    }

    @Test
    public void touchGetInstanceToEnsureFullCoverage() {
        assertNotNull(LibraryAndroidJava.getInstance());
    }
}