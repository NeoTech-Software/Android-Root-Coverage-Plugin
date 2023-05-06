package org.neotech.plugin.rootcoverage.util

import java.io.Closeable
import java.io.InputStream
import java.nio.charset.Charset


/**
 * Super quick and dirty "template engine" loosely based on mustache style templating.
 */
internal class SimpleTemplate {

    private val map = mutableMapOf<String, String>()

    fun putValue(key: String, value: String) {
        map[key] = value
    }

    fun process(inputStream: InputStream, charset: Charset): String {
        val content = inputStream.bufferedReader(charset).readLines()
        val result = mutableListOf<String>()
        content.forEach { line ->
            var adjustedLine = line
            map.forEach {
                adjustedLine = line.replace("{{${it.key}}}", it.value)
            }
            result.add(adjustedLine)
        }
        return result.joinToString(System.lineSeparator())
    }
}