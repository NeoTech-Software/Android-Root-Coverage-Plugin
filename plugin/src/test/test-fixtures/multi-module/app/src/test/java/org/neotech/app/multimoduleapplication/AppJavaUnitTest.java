package org.neotech.app.multimoduleapplication;

import org.junit.Test;
import org.neotech.app.AppJava;
import org.neotech.app.AppKotlin;
import org.neotech.library.android.LibraryAndroidJava;
import org.neotech.library.android.LibraryAndroidKotlin;

public class AppJavaUnitTest {

    @Test
    public void touchJavaCodeInApp() {
        AppJava.getInstance().touchedByJavaUnitTestInApp();
    }

    @Test
    public void touchKotlinCodeInApp() {
        AppKotlin.touchedByJavaUnitTestInApp();
    }

    @Test
    public void touchJavaCodeInLibrary() {
        LibraryAndroidJava.getInstance().touchedByJavaUnitTestInApp();
    }

    @Test
    public void touchKotlinCodeInLibrary() {
        LibraryAndroidKotlin.touchedByJavaUnitTestInApp();
    }
}
