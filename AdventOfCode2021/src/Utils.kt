package utils

import java.io.File

fun readFileAsLines(fileName: String): List<String> =
    File(fileName).useLines { it.toList() }

fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)