package org.neotech.app.multimoduleapplication

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.neotech.library.a.LibraryAKotlin
import org.neotech.library.b.LibraryBKotlin

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.text).text = "${LibraryAKotlin.getName()} ${LibraryBKotlin.getName()}"
    }
}