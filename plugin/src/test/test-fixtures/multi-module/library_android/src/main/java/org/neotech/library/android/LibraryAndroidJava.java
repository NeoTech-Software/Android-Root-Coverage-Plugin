package org.neotech.library.android;

public final class LibraryAndroidJava {

    private static LibraryAndroidJava INSTANCE;

    public static synchronized LibraryAndroidJava getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryAndroidJava();
        }
        return INSTANCE;
    }

    private LibraryAndroidJava() { }

    public String touchedByJavaInstrumentedTestInAndroidLibrary() {
        return "touchedByJavaInstrumentedTestInAndroidLibrary";
    }

    public String touchedByJavaUnitTestInAndroidLibrary() {
        return "touchedByJavaUnitTestInAndroidLibrary";
    }

    public String touchedByKotlinInstrumentedTestInAndroidLibrary() {
        return "touchedByKotlinInstrumentedTestInAndroidLibrary";
    }

    public String touchedByKotlinUnitTestInAndroidLibrary() {
        return "touchedByKotlinUnitTestInAndroidLibrary";
    }

    public String touchedByJavaInstrumentedTestInApp() {
        return "touchedByJavaInstrumentedTestInApp";
    }

    public String touchedByJavaUnitTestInApp() {
        return "touchedByJavaUnitTestInApp";
    }

    public String touchedByKotlinInstrumentedTestInApp() {
        return "touchedByKotlinInstrumentedTestInApp";
    }

    public String touchedByKotlinUnitTestInApp() {
        return "touchedByKotlinUnitTestInApp";
    }
}
