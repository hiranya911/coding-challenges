package day9

import utils.readFileAsLines
import java.util.*

fun main() {
    val hm = HeightMap.fromFile("inputs/d9_large.txt")
    println("Risk level = ${hm.riskLevel()}")
    println("3 largest basins = ${hm.threeLargestBasins()}")
}

val neighbors = listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0))

class HeightMap(private val points: Map<Pair<Int, Int>, Int>) {

    fun riskLevel(): Int =
        findLowPoints().sumOf { 1 + (points[it]!!) }

    fun threeLargestBasins(): Int =
        findLowPoints().map { findBasinSizeAt(it) }.sortedDescending().take(3)
            .fold(1) { total, next -> total * next }

    private fun findLowPoints(): List<Pair<Int, Int>> =
        points.keys.filter { isLowPoint(it) }

    private fun isLowPoint(p: Pair<Int, Int>): Boolean =
        neighbors.all { points[p]!! < (points[p.plus(it)] ?: 10) }

    private fun findBasinSizeAt(p: Pair<Int, Int>): Int {
        val frontier = Stack<Pair<Int, Int>>().apply { push(p) }
        val explored = mutableSetOf(p)
        while (!frontier.empty()) {
            val current = frontier.pop()
            successors(current).filter { it !in explored }.forEach {
                frontier.push(it)
                explored.add(it)
            }
        }

        return explored.size
    }

    private fun successors(p: Pair<Int, Int>): List<Pair<Int, Int>> =
        neighbors.map { p.plus(it) }.filter { (points[it] ?: -1) in (points[p]!! + 1)..8 }

    companion object {
        fun fromFile(fileName: String): HeightMap =
            readFileAsLines(fileName).let { lines ->
                return lines.indices
                    .flatMap { y -> lines[0].indices.map { Pair(it, y) to lines[y][it].digitToInt() } }
                    .associate { it.first to it.second }
                    .let { HeightMap(it) }
            }
    }
}

fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first + other.first, this.second + other.second)
