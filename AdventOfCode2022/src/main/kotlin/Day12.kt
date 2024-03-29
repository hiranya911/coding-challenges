package day12

import utils.readFileAsLines
import java.util.LinkedList
import java.util.Queue

fun main() {
    val locations = parseGridMap("inputs/d12_large.txt")
    val explorer = MapExplorer(locations)
    val start = locations.first { it.id == 'S' }
    println("Shortest path from start: ${explorer.shortestPathFrom(start)}")
    println("Shortest path: ${explorer.shortestPath()}")
}

fun parseGridMap(path: String): List<Location> {
    val lines = readFileAsLines(path)
    val locations = mutableListOf<Location>()
    for (y in 0..lines.lastIndex) {
        val line = lines[y]
        for (x in 0..line.lastIndex) {
            locations.add(Location(x, y, line[x]))
        }
    }

    return locations.toList()
}

class MapExplorer(private val locations: List<Location>) {

    private val byPosition: Map<Pair<Int, Int>, Location> = locations.associateBy { Pair(it.x, it.y) }

    fun shortestPathFrom(start: Location): Int {
        return bfs(start)?.distance() ?: Int.MAX_VALUE
    }

    fun shortestPath(): Int {
        val startingPoints = locations.filter { it.elevation == 0 }
        return startingPoints.minOf { shortestPathFrom(it) }
    }

    private fun bfs(start: Location): Node? {
        val frontier: Queue<Node> = LinkedList<Node>().apply { add(Node(start, parent = null)) }
        val explored = mutableSetOf(start)
        while (frontier.isNotEmpty()) {
            val current = frontier.remove()
            if (current.location.id == 'E') {
                return current
            }

            successors(current).forEach {
                if (it.location !in explored) {
                    explored.add(it.location)
                    frontier.add(it)
                }
            }
        }

        return null
    }

    private fun successors(node: Node): List<Node> {
        val x = node.location.x
        val y = node.location.y
        return listOf(
            Pair(x + 1, y),
            Pair(x - 1, y),
            Pair(x, y + 1),
            Pair(x, y - 1),
        ).mapNotNull {
            val loc = byPosition[it] ?: return@mapNotNull null
            if (loc.elevation > node.location.elevation + 1) return@mapNotNull null
            Node(loc, node)
        }
    }
}

data class Node(val location: Location, val parent: Node?) {
    fun distance(): Int {
        return parent?.let { 1 + it.distance() } ?: 0
    }
}

data class Location(val x: Int, val y: Int, val id: Char) {
    val elevation: Int = when (id) {
        'S' -> charToHeight('a')
        'E' -> charToHeight('z')
        else -> charToHeight(id)
    }

    companion object {
        private fun charToHeight(ch: Char): Int = ch.code - 'a'.code
    }
}