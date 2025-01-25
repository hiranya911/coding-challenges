package day10

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val grid = Grid.fromFile("inputs/day10.txt")
    println("Total score: ${grid.totalScore()}")
    println("Total rating: ${grid.totalRating()}")
}

data class Grid(
    private val locations: Map<Pair<Int, Int>, Int>,
) {
    private val trailheads = locations.filterValues { it == 0 }.keys

    fun totalScore(): Int {
        return trailheads.sumOf { score(it) }
    }

    fun totalRating(): Int {
        return trailheads.sumOf { score(it, disableTracking = true) }
    }

    private fun score(trailhead: Pair<Int, Int>, disableTracking: Boolean = false): Int {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val frontier = Stack<Node>()
        frontier.push(Node(trailhead, null))
        val paths = mutableListOf<Node>()

        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            val height = locations.getValue(current.pos)
            if (height == 9) {
                paths.add(current)
                continue
            }

            for (child in successors(current.pos)) {
                if (child in visited) continue
                // There's no risk of loops in this problem (the heights are strictly increasing).
                // So we can safely explore all paths without tracking visited nodes.
                if (!disableTracking) {
                    visited.add(child)
                }
                frontier.add(Node(child, current))
            }
        }

        return paths.size
    }

    private fun successors(pos: Pair<Int, Int>): Set<Pair<Int, Int>> {
        val neighbors = listOf(
            Pair(pos.first - 1, pos.second),
            Pair(pos.first + 1, pos.second),
            Pair(pos.first, pos.second - 1),
            Pair(pos.first, pos.second + 1),
        )
        val currentHeight = locations.getValue(pos)
        return neighbors.filter {
            val height = locations[it] ?: return@filter false
            height == currentHeight + 1
        }.toSet()
    }

    companion object {
        private const val IMPASSABLE = -1

        fun fromFile(filename: String): Grid {
            val locations = mutableMapOf<Pair<Int, Int>, Int>()
            val lines = readFileAsLines(filename)
            for (y in lines.indices) {
                val line = lines[y]
                for (x in line.indices) {
                    val ch = line[x]
                    val height = when (ch) {
                        '.' -> IMPASSABLE
                        else -> ch.digitToInt()
                    }
                    locations[Pair(x, y)] = height
                }
            }

            return Grid(locations)
        }
    }
}

private data class Node(
    val pos: Pair<Int, Int>,
    val prev: Node?,
)