package day2

import utils.readFileAsLines
import kotlin.math.abs

fun main() {
    val reports = readFileAsLines("inputs/day2.txt").map { Report.fromString(it) }
    println("Safe reports: ${reports.count { it.isSafe() }}")
    println("Safe reports with tolerance: ${reports.count { it.isSafeWithTolerance() }}")
}

data class Report(val levels: List<Int>) {
    fun isSafe(): Boolean {
        val deltas = levels.zipWithNext().map { (a, b) -> b - a }
        val inRange = deltas.all { abs(it) in (1..3) }
        val hasSameSign = deltas.map {
            when {
                it > 0 -> 1
                it < 0 -> -1
                else -> 0
            }
        }.distinct().size == 1
        return inRange && hasSameSign
    }

    fun isSafeWithTolerance(): Boolean {
        val safe = isSafe()
        if (safe) return true

        levels.indices.forEach {
            val modified = levels.filterIndexed { index, i -> index != it  }
            val temp = Report(modified)
            if (temp.isSafe()) return true
        }
        return false
    }

    companion object {
        fun fromString(s: String): Report {
            return Report(
                s.split(" ").map { it.toInt() }
            )
        }
    }
}