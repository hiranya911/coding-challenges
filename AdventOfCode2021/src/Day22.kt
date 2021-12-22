package day22

import utils.readFileAsLines
import java.lang.Integer.max
import kotlin.math.min

fun main() {
    val instructions = readFileAsLines("inputs/d22_large.txt").map {
        Pair(Cuboid.fromString(it), it.startsWith("on "))
    }

    val init = Cuboid(Pair(-50, 51), Pair(-50, 51), Pair(-50, 51))
    println("Active cubes within init region = ${initializeCore(instructions, init)}")
    println("Active cubes total = ${initializeCore(instructions)}")
}

fun initializeCore(instructions: List<Pair<Cuboid, Boolean>>, bounds: Cuboid? = null): Long {
    val reactor = ReactorCore()
    instructions.forEach {
        if (bounds == null || bounds.totallyContains(it.first)) {
            reactor.add(it.first, it.second)
        }
    }

    return reactor.activeCubes()
}

class Cuboid(private val x: Pair<Int, Int>, private val y: Pair<Int, Int>, private val z: Pair<Int, Int>) {

    fun size(): Long = (x.second - x.first).toLong() * (y.second - y.first) * (z.second - z.first)

    fun totallyContains(other: Cuboid): Boolean =
        (x.first <= other.x.first && x.second >= other.x.second) &&
                (y.first <= other.y.first && y.second >= other.y.second) &&
                (z.first <= other.z.first && z.second >= other.z.second)

    fun getIntersection(other: Cuboid): Cuboid? {
        val xmin = max(x.first, other.x.first)
        val xmax = min(x.second, other.x.second)
        val ymin = max(y.first, other.y.first)
        val ymax = min(y.second, other.y.second)
        val zmin = max(z.first, other.z.first)
        val zmax = min(z.second, other.z.second)
        if (xmin >= xmax || ymin >= ymax || zmin >= zmax) {
            return null
        }

        return Cuboid(Pair(xmin, xmax), Pair(ymin, ymax), Pair(zmin, zmax))
    }

    fun overlap(other: Cuboid): List<Cuboid> {
        getIntersection(other) ?: return listOf()
        val xpoints = sortedCoordinates(x, other.x)
        val ypoints = sortedCoordinates(y, other.y)
        val zpoints = sortedCoordinates(z, other.z)

        val result = mutableListOf<Cuboid>()
        for (i in 0 until 3) {
            val xmin = xpoints[i]
            val xmax = xpoints[i+1]
            for (j in 0 until 3) {
                val ymin = ypoints[j]
                val ymax = ypoints[j+1]
                for (k in 0 until 3) {
                    val zmin = zpoints[k]
                    val zmax = zpoints[k+1]
                    if (xmin < xmax && ymin < ymax && zmin < zmax) {
                        val cuboid = Cuboid(Pair(xmin, xmax), Pair(ymin, ymax), Pair(zmin, zmax))
                        if (this.totallyContains(cuboid) || other.totallyContains(cuboid)) {
                            result.add(cuboid)
                        }
                    }
                }
            }
        }

        return result
    }

    override fun toString(): String = "${x.first}..${x.second},${y.first}..${y.second},${z.first}..${z.second}"

    companion object {
        fun fromString(line: String): Cuboid {
            val segments = line.substringAfter(" ").split(",").map { parseSegment(it) }
            return Cuboid(segments[0], segments[1], segments[2])
        }

        private fun parseSegment(segment: String): Pair<Int, Int> {
            val range = segment.substringAfter("=")
            return Pair(range.substringBefore("..").toInt(), range.substringAfter("..").toInt() + 1)
        }
    }
}

fun sortedCoordinates(p1: Pair<Int, Int>, p2: Pair<Int, Int>): List<Int> =
    listOf(p1.first, p1.second, p2.first, p2.second).sorted()

class ReactorCore {

    private val map = mutableMapOf<Cuboid, Boolean>()

    fun add(cuboid: Cuboid, status: Boolean) {
        val toRemove = mutableSetOf<Cuboid>()
        val toAdd = mutableMapOf<Cuboid, Boolean>()
        for (entry in map) {
            if (entry.value == status && entry.key.totallyContains(cuboid)) {
                return
            }
            if (cuboid.totallyContains(entry.key)) {
                toRemove.add(entry.key)
            } else if (entry.key.getIntersection(cuboid) != null) {
                toRemove.add(entry.key)
                for (sub in entry.key.overlap(cuboid)) {
                    if (!cuboid.totallyContains(sub)) {
                        toAdd[sub] = entry.value
                    }
                }
            }
        }

        toRemove.forEach(map::remove)
        map.putAll(toAdd)
        map[cuboid] = status
    }

    fun activeCubes(): Long =
        map.filter { it.value }.map { it.key.size() }.sum()
}
