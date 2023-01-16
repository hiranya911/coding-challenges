package day4

import utils.readFileAsLines

typealias SectionRange = Pair<Int, Int>

fun main() {
    val pairs = readFileAsLines("inputs/d4_large.txt").map { parseSectionAssignment(it) }
    val fullyContained = pairs.count { fullyContains(it.first, it.second) }
    println("Fully contained pairs: $fullyContained")

    val overlapped = pairs.count { overlaps(it.first, it.second) }
    println("Overlapped pairs: $overlapped")
}

fun fullyContains(pair1: SectionRange, pair2: SectionRange): Boolean {
    if (pair1.first >= pair2.first && pair1.second <= pair2.second) return true
    if (pair2.first >= pair1.first && pair2.second <= pair1.second) return true
    return false
}

fun overlaps(a: SectionRange, b: SectionRange): Boolean {
    if (a.first >= b.first && a.first <= b.second) return true
    if (b.first >= a.first && b.first <= a.second) return true
    return false
}

fun parseSectionAssignment(line: String): Pair<SectionRange, SectionRange> {
    val (pair1, pair2) = line.split(",")
    return parseSectionRage(pair1) to parseSectionRage(pair2)
}

fun parseSectionRage(str: String): SectionRange {
    val (start, end) = str.split("-")
    return SectionRange(start.toInt(), end.toInt())
}