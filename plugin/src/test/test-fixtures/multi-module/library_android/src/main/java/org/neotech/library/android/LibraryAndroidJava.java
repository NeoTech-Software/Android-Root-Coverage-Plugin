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
        // Check if the BuildConfig file is available, this is here to prevent regression on:
        // https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/issues/54
        System.out.println(org.neotech.library.a.BuildConfig.DEBUG);
    }

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
