package org.neotech.plugin.rootcoverage.util

import java.io.OutputStream
import java.io.Writer
import java.nio.CharBuffer

/**
 * Gradle's forwardOutput() or even forwardStdOutput(OutputStreamWriter(System.out)) seems to be
 * adding extra new lines to the generated output in the console. This is a small fix for that, it
 * might not be the fastest, but it works.
 */
class SystemOutputWriter private constructor(private val output: OutputStream): Writer() {

    override fun write(buffer: CharArray, offset: Int, length: Int) {
        val charBuffer = CharBuffer.wrap(buffer, offset, length)
        output.write(Charsets.UTF_8.encode(charBuffer).array())
    }

    override fun flush() { }

    override fun close() { }

    companion object {
        fun out() = SystemOutputWriter(System.out)
        fun err() = SystemOutputWriter(System.err)
    }
}
