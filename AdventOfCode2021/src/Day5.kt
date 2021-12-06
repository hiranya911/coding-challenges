package day5

import utils.readFileAsLines

fun main() {
    val lines = parseLines("inputs/d5_large.txt")

    val counts1 = countIntersections(lines.filter { it.isVertical() || it.isHorizontal() })
    println("Two or more (horizontal & vertical) = ${counts1.values.count { it >= 2 }}")

    val counts2 = countIntersections(lines)
    println("Two or more = ${counts2.values.count { it >= 2 }}")
}

fun countIntersections(lines: List<Line>): Map<Pair<Int, Int>, Int> =
    lines.flatMap { it.points() }.groupingBy { it }.eachCount()

fun parseLines(fileName: String): List<Line> =
    readFileAsLines(fileName).map { Line.fromString(it) }

class Line(private val start: Pair<Int, Int>, private val end: Pair<Int, Int>) {

    fun isHorizontal(): Boolean = start.second == end.second
    fun isVertical(): Boolean = start.first == end.first

    fun points(): List<Pair<Int, Int>> =
        if (isHorizontal()) {
            xrange().map { Pair(it, start.second) }
        } else if (isVertical()) {
            yrange().map { Pair(start.first, it) }
        } else {
            xrange().zip(yrange())
        }

    private fun xrange(): IntProgression =
        if (start.first < end.first) start.first..end.first else start.first downTo end.first

    private fun yrange(): IntProgression =
        if (start.second < end.second) start.second..end.second else start.second downTo end.second

    companion object {
        fun fromString(str: String): Line =
            Line(
                parsePair(str.substringBefore(" ->")),
                parsePair(str.substringAfter("-> "))
            )
    }
}

fun parsePair(str: String): Pair<Int, Int> =
    str.split(",").let { Pair(it[0].toInt(), it[1].toInt()) }
