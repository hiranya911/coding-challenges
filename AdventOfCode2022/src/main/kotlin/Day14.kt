package day14

import utils.readFileAsLines
import kotlin.math.max
import kotlin.math.min

fun main() {
    val lines = readFileAsLines("inputs/d14_large.txt")
    println("Units of sand without floor: ${dropSandWithoutFloor(lines)}")
    println("Units of sand with floor: ${dropSandWithFloor(lines)}")
}

fun dropSandWithoutFloor(lines: List<String>): Int {
    val start = Pos(500, 0)
    var count = 0
    val cave = Cave.fromLines(lines)
    while (true) {
        if (cave.dropSandAt(start) == Result.SandOverboard) return count
        count++
    }
}

fun dropSandWithFloor(lines: List<String>): Int {
    val start = Pos(500, 0)
    var count = 0
    val cave = Cave.fromLines(lines, withFloor = true)
    while (true) {
        if (cave.dropSandAt(start) == Result.IngressObstructed) return count
        count++
    }
}

class Cave(private val map: MutableMap<Pos, Contents>, private val withFloor: Boolean) {

    private val maxY = map.keys.maxOf { it.y }

    fun dropSandAt(at: Pos): Result {
        var current = at
        if (get(current).isObstructed()) return Result.IngressObstructed
        while (true) {
            val next = getNext(current)
            if (next == null) {
                map[current] = Contents.Sand
                return Result.SandAtRest
            }
            if (next.y > maxY && !withFloor) return Result.SandOverboard
            current = next
        }
    }

    private fun getNext(current: Pos): Pos? {
        if (withFloor && current.y + 1 == maxY + 2) return null
        return listOf(
            Pos(current.x, current.y + 1),
            Pos(current.x - 1, current.y + 1),
            Pos(current.x + 1, current.y + 1),
        ).firstOrNull {
            !get(it.x, it.y).isObstructed()
        }
    }

    fun draw() {
        val yLimit = if (withFloor) maxY + 2 else maxY
        val minX = map.keys.minOf { it.x }
        val maxX = map.keys.maxOf { it.x }
        for (y in 0..yLimit) {
            for (x in minX..maxX) {
                print(get(x, y).symbol())
            }
            println()
        }
    }

    private fun get(x: Int, y: Int): Contents {
        return get(Pos(x, y))
    }

    private fun get(pos: Pos): Contents {
        return map[pos] ?: Contents.Air
    }

    companion object {
        fun fromLines(lines: List<String>, withFloor: Boolean = false): Cave {
            val map = mutableMapOf<Pos, Contents>()
            lines.forEach {  line ->
                val path = enumerateRockPath(line).associateWith { Contents.Rock }
                map.putAll(path)
            }

            return Cave(map, withFloor)
        }

        private fun enumerateRockPath(line: String): Set<Pos> {
            val endpoints = line.split(" -> ").map {
                val (x, y) = it.split(",")
                Pos(x.toInt(), y.toInt())
            }
            val path = mutableSetOf<Pos>()
            for (i in 0 until endpoints.lastIndex) {
                path.addAll(enumerateRockPath(endpoints[i], endpoints[i+1]))
            }

            return path
        }

        private fun enumerateRockPath(from: Pos, to: Pos): Set<Pos> {
            val points = mutableSetOf<Pos>()
            if (from.x != to.x) {
                for (x in min(from.x, to.x)..max(from.x, to.x)) {
                    points.add(Pos(x, from.y))
                }
            } else if (from.y != to.y) {
                for (y in min(from.y, to.y)..max(from.y, to.y)) {
                    points.add(Pos(from.x, y))
                }
            } else {
                throw IllegalArgumentException("Invalid endpoints: $from -> $to")
            }

            return points
        }
    }
}

data class Pos(val x: Int, val y: Int)
enum class Contents {
    Air, Rock, Sand;

    fun isObstructed(): Boolean = this != Air
    fun symbol(): String = when (this) {
        Air -> "."
        Rock -> "#"
        Sand -> "o"
    }
}

enum class Result {
    IngressObstructed,
    SandAtRest,
    SandOverboard,
}