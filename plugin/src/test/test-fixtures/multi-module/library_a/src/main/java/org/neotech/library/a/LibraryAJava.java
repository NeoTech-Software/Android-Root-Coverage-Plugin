package org.neotech.library.a;

public final class LibraryAJava {

    private static LibraryAJava INSTANCE;

    public static synchronized LibraryAJava getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LibraryAJava();
        }
        return INSTANCE;
    }

    private LibraryAJava() {

    }

    public String getName() {
        return "LibraryAJava";
    }
}
