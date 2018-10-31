package org.neotech.library.b;

public final class LibraryBJava {

    private static LibraryBJava INSTANCE;

    public static synchronized LibraryBJava getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LibraryBJava();
        }
        return INSTANCE;
    }

    private LibraryBJava() {

    }

    public String getName() {
        return "LibraryBJava";
    }
}
