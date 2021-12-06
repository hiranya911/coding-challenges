package day3

import utils.readFileAsLines

fun main() {
    val report = readDiagnosticReport("inputs/d3_large.txt")
    println("Power consumption = ${report.powerConsumption()}")
    println("Life support = ${report.lifeSupportRating()}")
}

fun readDiagnosticReport(fileName: String): DiagnosticReport =
    readFileAsLines(fileName).let {
        DiagnosticReport(it.first().length, it.map { line -> line.toInt(2) })
    }

data class DiagnosticReport(val width: Int, val entries: List<Int>) {
    fun powerConsumption(): Int {
        val mfb = mostFrequentBits(entries)
        return gamma(mfb) * epsilon(mfb)
    }

    fun lifeSupportRating(): Int {
        return oxygenRating() * co2Rating()
    }

    private fun oxygenRating(): Int {
        var o2 = entries
        var idx = 0
        while (o2.size > 1) {
            val mfb = mostFrequentBitsAt(o2, width - 1 - idx)
            val (ones, zeros) = o2.partition { nthBit(it, width - idx) == 1 }
            o2 = if (mfb != 0) { ones } else { zeros }
            idx++
        }

        return o2.first()
    }

    private fun co2Rating(): Int {
        var co2 = entries
        var idx = 0
        while (co2.size > 1) {
            val mfb = mostFrequentBitsAt(co2, width - 1 - idx)
            val (ones, zeros) = co2.partition { nthBit(it, width - idx) == 1 }
            co2 = if (mfb != 0) { zeros } else { ones }
            idx++
        }

        return co2.first()
    }

    private fun nthBit(num: Int, n: Int): Int = ((num shr n-1) and 1)

    private fun mostFrequentBits(numbers: List<Int>): Array<Int> =
        (width-1 downTo  0)
            .map { mostFrequentBitsAt(numbers, it) }
            .toTypedArray()

    private fun mostFrequentBitsAt(numbers: List<Int>, pos: Int): Int =
        countOnes(numbers, pos).let {
            Pair(it, numbers.size - it).moreCommon()
        }

    private fun gamma(mfb: Array<Int>): Int =
        mfb.joinToString(separator = "").toInt(2)

    private fun epsilon(mfb: Array<Int>): Int =
        mfb.map { it.inv() and 1 }.joinToString(separator = "").toInt(2)
}

fun Pair<Int, Int>.moreCommon(): Int = when {
    first > second -> 1
    second > first -> 0
    else -> 2
}

fun countOnes(nums: List<Int>, pos: Int): Int =
    nums.sumOf { (it shr pos) and 1 }