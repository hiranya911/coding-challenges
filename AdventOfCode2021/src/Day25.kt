package day25

import utils.readFileAsLines

fun main() {
    val seaFloor = SeaFloor.fromFile("inputs/d25_large.txt")
    println("Steps until stable = ${stepsToStabilize(seaFloor)}")
}

fun stepsToStabilize(init: SeaFloor): Int {
    var seaFloor = init
    var steps = 0
    while (true) {
        steps++
        val next = seaFloor.tick()
        if (seaFloor.hasChanged(next)) {
            seaFloor = next
        } else {
            return steps
        }
    }
}

class SeaFloor(
    private val right: Set<Pair<Int, Int>>,
    private val down: Set<Pair<Int, Int>>,
    private val cols: Int,
    private val rows: Int
) {
    fun tick(): SeaFloor {
        val newRight = right.map {
            val next = nextRight(it)
            if (next in right || next in down) it else next
        }.toSet()
        val newDown = down.map {
            val next = nextDown(it)
            if (next in newRight || next in down) it else next
        }.toSet()

        return SeaFloor(newRight, newDown, cols, rows)
    }

    fun hasChanged(other: SeaFloor): Boolean =
        right.size != other.right.size ||
                down.size != other.down.size ||
                right.size != right.intersect(other.right).size ||
                down.size != down.intersect(other.down).size

    private fun nextRight(p: Pair<Int, Int>): Pair<Int, Int> =
        Pair((p.first+1) % cols, p.second)

    private fun nextDown(p: Pair<Int, Int>): Pair<Int, Int> =
        Pair(p.first, (p.second+1) % rows)

    companion object {
        fun fromFile(fileName: String): SeaFloor {
            val lines = readFileAsLines(fileName)
            val right = mutableSetOf<Pair<Int, Int>>()
            val down = mutableSetOf<Pair<Int, Int>>()
            (0..lines.lastIndex)
                .flatMap { y -> (0..lines[0].lastIndex).map { x -> Pair(x, y) } }
                .forEach {
                    val symbol = lines[it.second][it.first]
                    if (symbol == '>') {
                        right.add(it)
                    } else if (symbol == 'v') {
                        down.add(it)
                    }
                }

            return SeaFloor(right, down, lines[0].length, lines.size)
        }
    }
}