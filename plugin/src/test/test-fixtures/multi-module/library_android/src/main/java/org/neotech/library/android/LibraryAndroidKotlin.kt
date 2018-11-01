package org.neotech.library.android

object LibraryAndroidKotlin {

    @JvmStatic
    fun getName(): String {
        return "LibraryAndroidKotlin"
    }

    // This methed will not be tested in this module, but is touched and tested by the app
    // module. This to proof that the coverage report shows it as "covered" because the
    // app module tests touched it.
    @JvmStatic
    fun getName2(): String {
        return "LibraryAndroidKotlin"
    }
}