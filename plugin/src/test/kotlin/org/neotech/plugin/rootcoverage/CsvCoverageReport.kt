package org.neotech.plugin.rootcoverage

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class CoverageReport private constructor(
        private val instructionMissedColumn: Int,
        private val branchMissedColumn: Int,
        private val packageColumn: Int,
        private val classColumn: Int,
        private val records: List<CSVRecord>) {
    
    private fun CSVRecord?.assertCoverage(missedBranches: Int = 0, missedInstructions: Int = 0) {
        kotlin.test.assertNotNull(this)
        assertEquals(missedInstructions, this[instructionMissedColumn]?.toInt())
        assertEquals(missedBranches, this[branchMissedColumn]?.toInt())
    }

    fun assertCoverage(packageName: String, className: String, missedBranches: Int = 0, missedInstructions: Int = 0) {
        find(packageName, className).assertCoverage(missedBranches, missedInstructions)
    }

    private fun find(packageName: String, className: String): CSVRecord? = records.find {
        it[packageColumn] == packageName && it[classColumn] == className
    }

    companion object {

        fun from(file: File): CoverageReport = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader()).use {
            CoverageReport(
                    it.headerMap["INSTRUCTION_MISSED"]!!,
                    it.headerMap["BRANCH_MISSED"]!!,
                    it.headerMap["PACKAGE"]!!,
                    it.headerMap["CLASS"]!!,
                    it.records)
        }
    }
}
