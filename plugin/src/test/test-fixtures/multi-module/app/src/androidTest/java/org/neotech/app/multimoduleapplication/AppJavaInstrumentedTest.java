package org.neotech.app.multimoduleapplication;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neotech.app.AppJava;
import org.neotech.app.AppKotlin;
import org.neotech.library.android.LibraryAndroidJava;
import org.neotech.library.android.LibraryAndroidKotlin;

@RunWith(AndroidJUnit4.class)
public class AppJavaInstrumentedTest {

    @Test
    public void touchJavaCodeInApp() {
        AppJava.getInstance().touchedByJavaInstrumentedTestInApp();
    }

    @Test
    public void touchKotlinCodeInApp() {
        AppKotlin.touchedByJavaInstrumentedTestInApp();
    }

    @Test
    public void touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByJavaInstrumentedTestInApp();
    }

    @Test
    public void touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByJavaInstrumentedTestInApp();
    }
}
