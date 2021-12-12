package day12

import utils.readFileAsLines
import java.util.*

fun main() {
    val caves = CaveMap.fromFile("inputs/d12_large.txt")
    println("Paths without small node repeats = ${caves.paths()}")
    println("Paths with 1 small node repeat = ${caves.paths(allowOneRepeat = true)}")
}

class CaveMap(private val graph: Map<String, List<String>>) {

    fun paths(allowOneRepeat: Boolean = false): Int {
        val start = Node("start")
        val frontier = Stack<Node>().apply { push(start) }
        val paths = mutableSetOf<Node>()
        while (!frontier.empty()) {
            val current = frontier.pop()
            if (current.name == "end") {
                paths.add(current)
            } else {
                successors(current).filter { current.isValid(it, allowOneRepeat) }.forEach {
                    frontier.push(it)
                }
            }
        }

        return paths.size
    }

    private fun successors(node: Node): List<Node> =
        graph[node.name]!!.map { Node(it, node) }

    companion object {
        fun fromFile(fileName: String): CaveMap =
            readFileAsLines(fileName)
                .flatMap {
                    val (src, dst) = Pair(it.substringBefore("-"), it.substringAfter("-"))
                    listOf(Pair(src, dst), Pair(dst, src))
                }
                .groupBy { it.first }
                .mapValues { it.value.map { item -> item.second } }
                .let { CaveMap(it) }
    }
}

val startOrEnd = setOf("start", "end")

data class Node(val name: String, val prev: Node? = null) {

    private val isLarge: Boolean = name[0].isUpperCase()
    private val smallNodeCounts: Map<String, Int> = (prev?.smallNodeCounts ?: mapOf()).let {
        if (!isLarge) {
            val result = it.toMutableMap()
            result[name] = (result[name] ?: 0) + 1
            result
        } else {
            it
        }
    }

    fun isValid(node: Node, allowOneRepeat: Boolean): Boolean {
        if (node.isLarge || node.name !in smallNodeCounts) {
            return true
        }

        return allowOneRepeat && node.name !in startOrEnd && smallNodeCounts.all { it.value <= 1 }
    }
}