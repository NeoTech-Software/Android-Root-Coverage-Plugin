package org.neotech.plugin.rootcoverage.util

import java.io.OutputStream
import java.io.Writer

/**
 * Gradle's forwardOutput() or even forwardStdOutput(OutputStreamWriter(System.out)) seems to be
 * adding extra new lines to the generated output in the console. This is a small fix for that, it
 * might not be the fastest, but it works.
 */
class SystemOutputWriter private constructor(private val output: OutputStream) : Writer() {

    private val bufferedWriter = output.bufferedWriter(Charsets.UTF_8)

    override fun write(buffer: CharArray, offset: Int, length: Int) {
        bufferedWriter.write(buffer, offset, length)
    }

    override fun flush() {
        bufferedWriter.flush()
    }

    override fun close() {
        bufferedWriter.close()
    }

    companion object {
        fun out() = SystemOutputWriter(System.out)
        fun err() = SystemOutputWriter(System.err)
    }
}
