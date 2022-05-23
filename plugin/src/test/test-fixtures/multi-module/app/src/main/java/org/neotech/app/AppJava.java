package org.neotech.app;

public final class AppJava {

    private static AppJava INSTANCE;

    public static synchronized AppJava getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppJava();
        }
        return INSTANCE;
    }

    private AppJava() {
        // Check if the BuildConfig file is available, this is here to prevent regression on:
        // https://github.com/NeoTech-Software/Android-Root-Coverage-Plugin/issues/54
        System.out.println(org.neotech.app.BuildConfig.DEBUG);
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
