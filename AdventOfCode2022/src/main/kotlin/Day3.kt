package day3

import utils.readFileAsLines

private val PRIORITY_ORDER: List<Char> = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray().toList()

fun main() {
    val lines = readFileAsLines("inputs/d3_large.txt")
    val checksum = lines.sumOf { findPackingError(it) }
    println("Packing error checksum = $checksum")

    val badgeSum = lines.chunked(3).sumOf { findBadgePriority(it) }
    println("Badge sum = $badgeSum")
}

fun findPackingError(str: String): Int {
    val mid = str.length / 2
    val part1 = str.substring(0, mid).toCharArray().toSet()
    val part2 = str.substring(mid).toCharArray().toSet()
    val common = part1.intersect(part2).single()
    return PRIORITY_ORDER.indexOf(common) + 1
}

fun findBadgePriority(lines: List<String>): Int {
    require(lines.size == 3)
    val common = lines.map { it.toCharArray().toSet() }.reduce { s1, s2 -> s1.intersect(s2) }.single()
    return PRIORITY_ORDER.indexOf(common) + 1
}


