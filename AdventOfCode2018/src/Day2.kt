package day2

import day1.readFileAsLines

fun main() {
    val lines = readFileAsLines("inputs/d2_large.txt")
    val histograms = lines.map { characterCount(it) }
    val twoOfAnyLetter = histograms.count { 2 in it }
    val threeOfAnyLetter = histograms.count { 3 in it }
    println("Checksum = ${twoOfAnyLetter * threeOfAnyLetter}")
    println("Common letters = ${findSimilar(lines)}")
}

fun characterCount(s: String): Map<Int, Int> =
    s.groupingBy { it }.eachCount().values.groupingBy { it }.eachCount()

fun diff(s1: String, s2: String): Int =
    s1.zip(s2).count { it.first != it.second }

fun findSimilar(words: List<String>): String {
    for (i in 0 until words.lastIndex) {
        for (j in i+1..words.lastIndex) {
            if (diff(words[i], words[j]) == 1) {
                return words[i].zip(words[j]).filter { it.first == it.second }.map{ it.first }.joinToString("")
            }
        }
    }

    throw Exception("no match found")
}
