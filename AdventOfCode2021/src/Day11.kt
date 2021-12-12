package day11

import utils.plus
import utils.readFileAsLines

fun main() {
    val om = OctopusMap.fromFile("inputs/d11_large.txt")
    val flashes = generateSequence { om.tick() }.takeWhileInclusive { it < om.size }.toList()
    println("Total flashes after 100 steps = ${ flashes.take(100).sum() }")
    println("All flashed at step = ${flashes.size}")
}

val neighbors = listOf(
    Pair(-1, 1), Pair(0, 1), Pair(1, 1),
    Pair(-1, 0), Pair(1, 0),
    Pair(-1, -1), Pair(0, -1), Pair(1, -1)
)

class OctopusMap(private var map: Map<Pair<Int, Int>, Int>) {

    val size: Int = map.size

    fun tick(): Int {
        val temp = map.keys.associateWith { map[it]!! + 1 }.toMutableMap()
        val flashed = mutableSetOf<Pair<Int, Int>>()
        while (true) {
            val moreThanNine = temp.filter { it.value > 9 && it.key !in flashed }
            if (moreThanNine.isEmpty()) {
                flashed.forEach { temp[it] = 0 }
                map = temp
                return flashed.size
            }

            moreThanNine.keys.forEach {
                flashed.add(it)
                adjacent(it).forEach { nbr -> temp[nbr] = temp[nbr]!! + 1 }
            }
        }
    }

    private fun adjacent(p: Pair<Int, Int>): List<Pair<Int, Int>> =
        neighbors.map { p.plus(it) }.filter { map.containsKey(it) }

    companion object {
        fun fromFile(fileName: String): OctopusMap {
            val lines = readFileAsLines(fileName)
            return lines.indices
                .flatMap { y -> lines[0].indices.map { x -> Pair(x, y) to lines[y][x].digitToInt() } }
                .associate { it.first to it.second }
                .let { OctopusMap(it) }
        }
    }
}

fun <T> Sequence<T>.takeWhileInclusive(pred: (T) -> Boolean): Sequence<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = pred(it)
        result
    }
}