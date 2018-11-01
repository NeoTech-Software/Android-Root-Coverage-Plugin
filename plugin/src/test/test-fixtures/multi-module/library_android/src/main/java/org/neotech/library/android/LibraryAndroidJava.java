package org.neotech.library.android;

public final class LibraryAndroidJava {

    private static LibraryAndroidJava INSTANCE;

    public static synchronized LibraryAndroidJava getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LibraryAndroidJava();
        }
        return INSTANCE;
    }

    private LibraryAndroidJava() {

    }

    public String getName() {
        return "LibraryAndroidJava";
    }

    // This methed will not be tested in this module, but is touched and tested by the app
    // module. This to proof that the coverage report shows it as "covered" because the
    // app module tests touched it.
    public String getName2() {
        return "LibraryAndroidJava";
    }
}
