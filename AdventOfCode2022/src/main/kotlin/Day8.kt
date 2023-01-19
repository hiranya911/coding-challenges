package day8

import utils.readFileAsLines

fun main() {
    val grid = Grid.fromLines(readFileAsLines("inputs/d8_large.txt"))
    println("Visible trees: ${grid.visibleTrees()}")
    println("Max scenic score: ${grid.maxScenicScore()}")
}

data class Grid(private val rows: List<List<Int>>) {
    private val rowCount: Int = rows.size
    private val columnCount: Int = rows[0].size

    companion object {
        fun fromLines(lines: List<String>): Grid {
            return Grid(lines.map { line -> line.map { it.digitToInt() } })
        }
    }

    fun maxScenicScore(): Int {
        fun scenicScore(x: Int, y: Int): Int {
            if (x == 0 || x == columnCount - 1) return 0
            if (y == 0 || y == rowCount - 1) return 0
            val current = lookup(x, y)
            val left = (x-1 downTo 0).takeWhileInclusive { lookup(it, y) < current }.size
            val right = (x+1 until columnCount).takeWhileInclusive { lookup(it, y) < current }.size
            val up = (y-1 downTo 0).takeWhileInclusive { lookup(x, it) < current }.size
            val down = (y+1 until rowCount).takeWhileInclusive { lookup(x, it) < current }.size
            return left * right * up * down
        }

        return (0 until columnCount).flatMap { x -> (0 until rowCount).map { y -> Pair(x, y) } }
            .maxOf { scenicScore(it.first, it.second) }
    }

    fun visibleTrees(): Int {
        fun isVisible(x: Int, y: Int): Boolean {
            if (x == 0 || x == columnCount - 1) return true
            if (y == 0 || y == rowCount - 1) return true
            val curr = lookup(x, y)
            if ((0 until x).all { lookup(it, y) < curr }) return true
            if ((x + 1 until columnCount).all { lookup(it, y) < curr }) return true
            if ((0 until y).all { lookup(x, it) < curr }) return true
            if ((y + 1 until rowCount).all { lookup(x, it) < curr }) return true
            return false
        }

        return (0 until columnCount).flatMap { x -> (0 until rowCount).map { y -> Pair(x, y) } }
            .count { isVisible(it.first, it.second) }
    }

    private fun lookup(x: Int, y: Int): Int {
        return rows[y][x]
    }
}

inline fun <T> Iterable<out T>.takeWhileInclusive(
    predicate: (T) -> Boolean
): List<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = predicate(it)
        result
    }
}