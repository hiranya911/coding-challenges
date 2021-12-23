package day3

import day1.readFileAsLines
import kotlin.math.max
import kotlin.math.min

fun main() {
    val rectangles = readFileAsLines("inputs/d3_large.txt").map { Rectangle.fromString(it) }
    val acc = Acc()
    rectangles.forEach(acc::add)
    println("Overlapping area = ${acc.overlapArea()}")
    println("None overlapping ID = ${findNonOverlapping(rectangles).id}")
}

fun findNonOverlapping(rectangles: List<Rectangle>): Rectangle {
    for (r in rectangles) {
        val overlapped = rectangles.minus(r).any { r.intersects(it) }
        if (!overlapped) {
            return r
        }
    }

    throw Exception("no match found")
}

class Acc {
    private val rectangles = mutableListOf<Rectangle>()
    private val overlaps = mutableListOf<Rectangle>()

    fun add(rect: Rectangle) {
        rectangles.mapNotNull { it.intersection(rect) }.forEach { deduplicate(it) }
        rectangles.add(rect)
    }

    fun overlapArea(): Int = overlaps.sumOf { it.area() }

    private fun deduplicate(section: Rectangle) {
        val toRemove = mutableSetOf<Rectangle>()
        val toAdd = mutableSetOf<Rectangle>()
        for (overlap in overlaps) {
            if (overlap.totallyContains(section)) {
                return
            }

            if (section.totallyContains(overlap)) {
                toRemove.add(overlap)
            } else if (overlap.intersects(section)) {
                toRemove.add(overlap)
                for (sub in overlap.overlap(section)) {
                    if (!section.totallyContains(sub)) {
                        toAdd.add(sub)
                    }
                }
            }
        }

        toRemove.forEach(overlaps::remove)
        overlaps.addAll(toAdd)
        overlaps.add(section)
    }
}

class Rectangle(val id: String, private val x: Pair<Int, Int>, private val y: Pair<Int, Int>) {
    fun area(): Int = (x.second - x.first) * (y.second - y.first)

    fun totallyContains(other: Rectangle): Boolean =
        (x.first <= other.x.first && x.second >= other.x.second) &&
                (y.first <= other.y.first && y.second >= other.y.second)

    fun intersection(other: Rectangle): Rectangle? {
        val xmin = Integer.max(x.first, other.x.first)
        val xmax = min(x.second, other.x.second)
        val ymin = Integer.max(y.first, other.y.first)
        val ymax = min(y.second, other.y.second)
        if (xmin < xmax && ymin < ymax) {
            return Rectangle(id + other.id, Pair(xmin, xmax), Pair(ymin, ymax))
        }

        return null
    }

    fun intersects(other: Rectangle): Boolean {
        val xmin = max(x.first, other.x.first)
        val xmax = min(x.second, other.x.second)
        val ymin = max(y.first, other.y.first)
        val ymax = min(y.second, other.y.second)
        return xmin < xmax && ymin < ymax
    }

    fun overlap(other: Rectangle): List<Rectangle> {
        if (!intersects(other)) return listOf()
        val xpoints = sortedCoordinates(x, other.x)
        val ypoints = sortedCoordinates(y, other.y)
        val result = mutableListOf<Rectangle>()
        xpoints.pairs().forEach { xpair ->
            ypoints.pairs().forEach { ypair ->
                val rect = Rectangle(id + other.id, xpair, ypair)
                if (this.totallyContains(rect) || other.totallyContains(rect)) {
                    result.add(rect)
                }
            }
        }

        return result
    }

    companion object {
        fun fromString(line: String): Rectangle {
            val id = line.substringBefore(" @")
            val origin = line.substringAfter("@ ").substringBefore(":").let {
                Pair(it.substringBefore(",").toInt(), it.substringAfter(",").toInt())
            }
            val length = line.substringAfter(": ").let {
                Pair(it.substringBefore("x").toInt(), it.substringAfter("x").toInt())
            }

            return Rectangle(
                id,
                Pair(origin.first, origin.first + length.first),
                Pair(origin.second, origin.second + length.second)
            )
        }
    }
}

fun sortedCoordinates(p1: Pair<Int, Int>, p2: Pair<Int, Int>): List<Int> =
    listOf(p1.first, p1.second, p2.first, p2.second).sorted()

fun List<Int>.pairs(): List<Pair<Int, Int>> =
    (0 until lastIndex).map { Pair(this[it], this[it+1]) }.filter { it.first < it.second }