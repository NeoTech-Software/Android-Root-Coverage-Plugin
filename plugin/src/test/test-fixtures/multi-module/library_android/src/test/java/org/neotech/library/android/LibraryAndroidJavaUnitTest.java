package org.neotech.library.android;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LibraryAndroidJavaUnitTest {

    @Test
    public void touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByJavaUnitTestInAndroidLibrary();
    }

    @Test
    public void touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByJavaUnitTestInAndroidLibrary();
    }

    @Test
    public void touchGetInstanceToEnsureFullCoverage() {
        assertNotNull(LibraryAndroidJava.getInstance());
    }
}