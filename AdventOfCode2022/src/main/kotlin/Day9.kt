package day9

import utils.readFileAsLines
import kotlin.math.abs

fun main() {
    val moves = readFileAsLines("inputs/d9_large.txt").map {
        val (direction, distance) = it.split(" ")
        direction to distance.toInt()
    }

    println("Tail positions (2 knots): ${simulateRopeChain(moves, knots = 2)}")
    println("Tail positions (10 knots): ${simulateRopeChain(moves, knots = 10)}")
}

private fun simulateRopeChain(moves: List<Pair<String, Int>>, knots: Int): Int {
    val chain = (0 until knots - 1).map { Rope() }
    val tailPath = mutableSetOf<Pos>()
    for ((direction, distance) in moves) {
        repeat(distance) {
            val first = chain.first()
            first.moveHead(direction)
            var nextHead = first.tail
            for (rope in chain.drop(1)) {
                rope.moveHeadTo(nextHead)
                nextHead = rope.tail
            }

            tailPath.add(nextHead)
        }
    }

    return tailPath.size
}

data class Pos(val x: Int, val y: Int) {
    fun move(direction: String): Pos {
        return when (direction) {
            "R" -> Pos(x+1, y)
            "L" -> Pos(x-1, y)
            "U" -> Pos(x, y+1)
            "D" -> Pos(x, y-1)
            else -> throw IllegalArgumentException("Invalid direction: $direction")
        }
    }

    fun isAdjacent(other: Pos): Boolean {
        return abs(x - other.x) <= 1 && abs(y - other.y) <= 1
    }
}

class Rope {
    private var head = Pos(0, 0)
    var tail = Pos(0, 0)

    fun moveHead(direction: String) {
        head = head.move(direction)
        tail = relocateTail(head, tail)
    }

    fun moveHeadTo(pos: Pos) {
        head = pos
        tail = relocateTail(head, tail)
    }
}

fun relocateTail(head: Pos, tail: Pos): Pos {
    if (head.isAdjacent(tail)) return tail
    var temp = tail
    if (head.y > tail.y) {
        temp = temp.move("U")
    } else if (head.y < tail.y) {
        temp = temp.move("D")
    }

    if (head.x > tail.x) {
        temp = temp.move("R")
    } else if (head.x < tail.x) {
        temp = temp.move("L")
    }

    return temp
}