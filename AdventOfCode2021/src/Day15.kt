package day15

import utils.plus
import utils.readFileAsLines
import java.lang.Exception
import java.util.*

fun main() {
    val cavern = Cavern.fromFile("inputs/d15_large.txt")
    println("Shortest path = ${cavern.shortestPath()}")

    val extendedCavern = cavern.extend()
    println("Shortest path in extended map = ${extendedCavern.shortestPath()}")
}

open class Cavern(protected val map: Map<Pair<Int, Int>, Int>, private val limx: Int, private val limy: Int) {

    private val start = Pair(0, 0)
    private val goal = Pair(limx, limy)

    fun shortestPath(): Int {
        val frontier = PriorityQueue(compareByCost).apply {
            add(Node(start,0))
        }
        val explored = mutableMapOf(start to 0)
        while (frontier.isNotEmpty()) {
            val current = frontier.remove()
            if (current.loc == goal) {
                return pathCost(current)
            }

            successors(current.loc).forEach {
                val newCost = current.cost + getCost(it)
                if (it !in explored || explored[it]!! > newCost) {
                    explored[it] = newCost
                    frontier.add(Node(it, newCost, current))
                }
            }
        }

        throw Exception("failed to find a path to goal")
    }

    open fun getCost(p: Pair<Int, Int>): Int = map[p]!!

    fun extend(factor: Int = 5): Cavern = ExtendedCavern(map, limx, limy, factor)

    private fun pathCost(node: Node): Int =
        generateSequence(node) { it.prev }.sumOf { getCost(it.loc) } - map[start]!!

    private fun successors(p: Pair<Int, Int>): List<Pair<Int, Int>> =
        neighbors.map { p.plus(it) }.filter { it.first in 0..limx && it.second in 0..limy }

    companion object {
        fun fromFile(fileName: String): Cavern {
            val lines = readFileAsLines(fileName)
            val map = mutableMapOf<Pair<Int, Int>, Int>()
            for (y in 0..lines.lastIndex) {
                for (x in 0..lines[0].lastIndex) {
                    map[Pair(x, y)] = lines[y][x].digitToInt()
                }
            }

            return Cavern(map, lines[0].lastIndex, lines.lastIndex)
        }
    }
}

fun Int.extendBy(factor: Int): Int = (this + 1) * factor - 1
fun Int.wrapToOne(): Int = if (this > 9) (this % 10) + 1 else this

class ExtendedCavern(map: Map<Pair<Int, Int>, Int>, limx: Int, limy: Int, factor: Int):
    Cavern(map, limx.extendBy(factor), limy.extendBy(factor)) {

    private val t0cols = limx + 1
    private val t0rows = limy + 1

    override fun getCost(p: Pair<Int, Int>): Int {
        val x = p.first % t0cols
        val xdiff = p.first / t0cols
        val y = p.second % t0rows
        val ydiff = p.second / t0rows
        return (map[Pair(x, y)]!! + xdiff + ydiff).wrapToOne()
    }
}

val neighbors = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))

class Node(val loc: Pair<Int, Int>, val cost: Int, val prev: Node? = null)

val compareByCost: Comparator<Node> = compareBy { it.cost }