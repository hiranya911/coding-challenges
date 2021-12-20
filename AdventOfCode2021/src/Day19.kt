package day19

import utils.readFileAsLines
import kotlin.math.abs
import kotlin.math.min

fun main() {
    val scanners = parseReport("inputs/d19_large.txt")
    val resolved = resolveScanners(scanners)
    val points = resolved.map { it.beacons.toSet() }.reduce { acc, next -> acc.union(next) }
    println("Total points = ${points.size}")

    val maxDistance = resolved.map { it.root }.let { roots ->
        roots.flatMap { a -> roots.map { b -> a.manhattanDistance(b) } }.maxOf { it }
    }
    println("Max distance = $maxDistance")
}

fun resolveScanners(scanners: List<Scanner>): Set<ResolvedScanner> {
    val resolved = mutableSetOf<ResolvedScanner>().apply {
        add(ResolvedScanner(0, scanners[0].beacons, Point(0, 0, 0)))
    }

    var unresolved = scanners.drop(1).flatMap { it.rotations() }
    while (unresolved.isNotEmpty()) {
        val (known, matched) = findOverlap(resolved, unresolved)
        unresolved = unresolved.filterNot { it.idx == matched.idx }
        resolved.add(known.reconcile(matched))
    }

    return resolved
}

fun findOverlap(resolved: Iterable<Scanner>, unresolved: Iterable<Scanner>): Pair<Scanner, Scanner> {
    for (s1 in resolved) {
        val s2 = unresolved.firstOrNull { s1.hasOverlap(it) }
        if (s2 != null) return Pair(s1, s2)
    }

    throw Exception("no overlapping regions")
}

typealias Point = Triple<Int, Int, Int>

open class Scanner(val idx: Int, val beacons: List<Point>) {

    private val distances = pairwiseDistances()

    fun hasOverlap(other: Scanner): Boolean {
        val result = distances.intersect(other.distances)
            .sumOf { x -> min(distances.count {it == x}, other.distances.count {it == x}) }
        return result >= 66 // 12 common points will have at least this many pairwise connections.
    }

    fun rotations(): List<Scanner> =
        (0..23).map { ori -> Scanner(idx, beacons.map { it.rotate(ori) })}

    fun reconcile(other: Scanner): ResolvedScanner {
        for (rotation in other.rotations()) {
            for (point in rotation.beacons) {
                val resolved = resolveScannerPosition(point, rotation)
                if (resolved != null) {
                    return resolved
                }
            }
        }

        throw Exception("reconcile failed")
    }

    private fun resolveScannerPosition(point: Point, other: Scanner): ResolvedScanner? {
        for (target in beacons) {
            val pos = target.subtract(point)
            val projections = other.beacons.map { pos.plus(it) }
            if (projections.intersect(beacons).count() >= 12) {
                return ResolvedScanner(other.idx, projections, pos)
            }
        }

        return null
    }

    private fun pairwiseDistances(): List<Point> =
        (0 until beacons.lastIndex)
            .flatMap { i -> (i+1..beacons.lastIndex).map { j -> beacons[i].relativeDistance(beacons[j]) } }
}

class ResolvedScanner(idx: Int, beacons: List<Point>, val root: Point): Scanner(idx, beacons)

fun Point.rotate(orientation: Int): Point =
    when (orientation) {
        0 -> this
        1 -> Point(second, third, first)
        2 -> Point(-second, first, third)
        3 -> Point(-first, -second, third)
        4 -> Point(second, -first, third)
        5 -> Point(third, second, -first)
        6 -> Point(third, first, second)
        7 -> Point(third, -second, first)
        8 -> Point(third, -first, -second)
        9 -> Point(-first, second, -third)
        10 -> Point(second, first, -third)
        11 -> Point(first, -second, -third)
        12 -> Point(-second, -first, -third)
        13 -> Point(-third, second, first)
        14 -> Point(-third, first, -second)
        15 -> Point(-third, -second, -first)
        16 -> Point(-third, -first, second)
        17 -> Point(first, -third, second)
        18 -> Point(-second, -third, first)
        19 -> Point(-first, -third, -second)
        20 -> Point(second, -third, -first)
        21 -> Point(first, third, -second)
        22 -> Point(-second, third, -first)
        23 -> Point(-first, third, second)
        else -> throw Exception("unknown orientation: $orientation")
    }

fun Point.plus(other: Point): Point =
    Point(first + other.first, second + other.second, third + other.third)

fun Point.subtract(other: Point): Point =
    Point(first - other.first, second - other.second, third - other.third)

fun Point.relativeDistance(other: Point): Point =
    Point(abs(first - other.first), abs(second - other.second), abs(third - other.third))

fun Point.manhattanDistance(other: Point): Int =
    abs(first - other.first) + abs(second - other.second) + abs(third - other.third)

fun parseReport(fileName: String): List<Scanner> {
    val lines = readFileAsLines(fileName)
    val scanners = mutableListOf<Scanner>()
    var beacons = mutableListOf<Point>()
    for (line in lines) {
        if (line.startsWith("--- scanner")) {
            if (beacons.isNotEmpty()) {
                scanners.add(Scanner(scanners.size, beacons))
                beacons = mutableListOf()
            }
        } else if (line != "") {
            line.split(",").also {
                beacons.add(Triple(it[0].toInt(), it[1].toInt(), it[2].toInt()))
            }
        }
    }

    if (beacons.isNotEmpty()) {
        scanners.add(Scanner(scanners.size, beacons))
        beacons = mutableListOf()
    }

    return scanners
}
