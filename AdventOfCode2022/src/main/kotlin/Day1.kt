package day1

import utils.readFileAsLines

fun main() {
    val lines = readFileAsLines("inputs/d1_large.txt")
    val counts = orderedCalorieCounts(lines)
    println("Max calories: ${counts.last()}")

    val top3Total = counts.takeLast(3).sum()
    println("Top 3 total: $top3Total")
}

fun orderedCalorieCounts(lines: List<String>): List<Int> {
    val result = mutableListOf<Int>()
    var sum = 0
    for (line in lines) {
        if (line.isEmpty()) {
            result.add(sum)
            sum = 0
        } else {
            sum += line.toInt()
        }
    }

    result.add(sum)
    return result.sorted()
}