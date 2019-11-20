package org.neotech.app.multimoduleapplication;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.neotech.library.android.LibraryAndroidJava;
import org.neotech.library.android.LibraryAndroidKotlin;

public class TouchLibraryCodeFromJavaUnitTest {

    @Test
    public void touchJavaCode() {
        LibraryAndroidJava.getInstance().touchedByUnitTestInConsumer();
    }

    @Test
    public void touchKotlinCode() {
        LibraryAndroidKotlin.touchedByUnitTestInConsumer();
    }
}
