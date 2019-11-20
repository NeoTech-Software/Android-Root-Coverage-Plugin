package org.neotech.library.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LibraryAndroidInstrumentedTest {

    @Test
    public void touchJavaCode() {
        LibraryAndroidJava.getInstance().touchedByAndroidTest();
    }

    @Test
    public void touchKotlinCode() {
        LibraryAndroidKotlin.touchedByAndroidTest();
    }

    @Test
    public void touchGetInstanceToEnsureFullCoverage() {
        assertNotNull(LibraryAndroidJava.getInstance());
    }
}
