package org.neotech.plugin.rootcoverage.util

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

        val mapWithBrackets = map.mapKeys {
            "{{${it.key}}}"
        }
        val keysWithBrackets = mapWithBrackets.keys

        content.forEach { line ->
            val adjustedLine = StringBuilder(line)

            var startIndex = 0
            do {
                val indexOf = adjustedLine.findAnyOf(keysWithBrackets, startIndex = startIndex)
                if (indexOf == null) {
                    startIndex = -1
                } else {

                    // Calculate how far indented this replacement is on the current line
                    val lineStart = adjustedLine.lastIndexOf("\n", indexOf.first)
                    val indent = if (lineStart == -1) {
                        indexOf.first // First line
                    } else {
                        indexOf.first - lineStart // Any other line
                    }

                    // Get the replacement string and indent it
                    val replacement = mapWithBrackets[indexOf.second]!!.prependWhitespaceIndentExceptFirstLine(indent)

                    adjustedLine.replace(indexOf.first, indexOf.first + indexOf.second.length, replacement)
                    startIndex += replacement.length
                }
            } while (startIndex != -1)

            result.add(adjustedLine.toString())
        }
        return result.joinToString(System.lineSeparator())
    }
}

private fun String.prependWhitespaceIndentExceptFirstLine(amount: Int): String {
    val indent = " ".repeat(amount)
    return lineSequence().mapIndexed { index: Int, line: String ->
        if (index == 0) {
            line
        } else {
            indent + line
        }
    }.joinToString("\n")
}