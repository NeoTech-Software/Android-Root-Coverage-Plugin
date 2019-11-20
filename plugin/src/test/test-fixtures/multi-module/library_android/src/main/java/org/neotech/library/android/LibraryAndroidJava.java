package org.neotech.library.android;

public final class LibraryAndroidJava {

    private static LibraryAndroidJava INSTANCE;

    public static synchronized LibraryAndroidJava getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryAndroidJava();
        }
        return INSTANCE;
    }

    private LibraryAndroidJava() {

    }

    public String touchedByUnitTest() {
        return "touchedByUnitTest";
    }

    public String touchedByAndroidTest() {
        return "touchedByAndroidTest";
    }

    public String touchedByAndroidTestInConsumer() {
        return "touchedByAndroidTestInConsumer";
    }
}
