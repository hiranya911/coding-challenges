package utils

import java.io.File
import kotlin.math.pow

fun readFileAsLines(fileName: String): List<String> =
    File(fileName).useLines { it.toList() }

fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

fun Int.pow(n: Int): Int =
    this.toDouble().pow(n).toInt()