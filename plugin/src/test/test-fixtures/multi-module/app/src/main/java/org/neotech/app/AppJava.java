package org.neotech.app;

public final class AppJava {

    private static AppJava INSTANCE;

    public static synchronized AppJava getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppJava();
        }
        return INSTANCE;
    }

    private AppJava() { }

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
