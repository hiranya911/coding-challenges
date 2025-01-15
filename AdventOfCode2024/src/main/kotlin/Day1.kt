package day1

import utils.readFileAsLines
import kotlin.math.abs

fun main() {
    val (lst1, lst2) = buildLists("inputs/day1.txt")
    println("Total distance: ${getTotalDistance(lst1, lst2)}")
    println("Similarity score: ${getSimilarityScore(lst1, lst2)}")
}

fun buildLists(filename: String): Pair<List<Int>, List<Int>> {
    val lines = readFileAsLines(filename)
    val lst1 = mutableListOf<Int>()
    val lst2 = mutableListOf<Int>()
    for (line in lines) {
        val (x, y) = line.split("\\s+".toRegex()).map { it.toInt() }
        lst1.add(x)
        lst2.add(y)
    }
    return Pair(lst1, lst2)
}

fun getTotalDistance(lst1: List<Int>, lst2: List<Int>): Int {
    val sorted1 = lst1.sorted()
    val sorted2 = lst2.sorted()
    require(sorted1.size == sorted2.size)
    return sorted1.zip(sorted2).sumOf { (a, b) -> abs(a - b) }
}

fun getSimilarityScore(lst1: List<Int>, lst2: List<Int>): Int {
    val freq = lst2.groupingBy { it }.eachCount()
    return lst1.sumOf { it * freq.getOrDefault(it, 0) }
}