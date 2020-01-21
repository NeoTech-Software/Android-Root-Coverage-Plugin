package org.neotech.library.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LibraryAndroidJavaInstrumentedTest {

    @Test
    public void touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByJavaInstrumentedTestInAndroidLibrary();
    }

    @Test
    public void touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByJavaInstrumentedTestInAndroidLibrary();
    }

    @Test
    public void touchGetInstanceToEnsureFullCoverage() {
        assertNotNull(LibraryAndroidJava.getInstance());
    }
}
