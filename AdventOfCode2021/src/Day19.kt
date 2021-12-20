package day19

import utils.readFileAsLines
import kotlin.math.abs
import kotlin.math.min

fun main() {
    val scanners = parseReport("inputs/d19_large.txt")
    println("======")

    val points = mutableSetOf<Point>()
    points.addAll(scanners[0].beacons)

    val done = mutableSetOf<Scanner>()
    done.add(scanners[0])

    val roots = mutableSetOf<Point>()

    var unfixed = scanners.drop(1).toMutableList()
        .flatMap { scn -> (0..23).map{ ori -> Scanner(scn.idx, scn.beacons.map { it.rotate(ori) }) } }
    while (unfixed.isNotEmpty()) {
        val (known, matched) = findOverlap(done, unfixed)
        unfixed = unfixed.filter { it.idx != matched.idx }

        val resolved = known.reconcile(matched)
        //println(resolved.scanner.beacons)
        done.add(resolved.scanner)
        roots.add(resolved.pos)
        points.addAll(resolved.scanner.beacons)
    }

    println("Total points = ${points.size}")

    val distances = mutableListOf<Int>()
    for (a in roots) {
        for (b in roots) {
            distances.add(a.manhattanDistance(b))
        }
    }

    println("Max distance = ${distances.maxOf { it }}")
}

fun findOverlap(matched: Iterable<Scanner>, unfixed: List<Scanner>): Pair<Scanner, Scanner> {
    for (s1 in matched) {
        for (s2 in unfixed) {
            if (s1.hasOverlap(s2)) {
                return Pair(s1, s2)
            }
        }
    }

    throw Exception("oops")
}

typealias Point = Triple<Int, Int, Int>

class Scanner(val idx: Int, val beacons: List<Point>) {

    val distances = pairwiseDistances()

    fun hasOverlap(other: Scanner): Boolean {
        val result = distances.intersect(other.distances).sumOf {
                x -> min(distances.count {it == x}, other.distances.count {it == x})
        }
        return result >= 66
    }

    fun rotations(): List<Scanner> =
        (0..23).map { ori -> Scanner(idx, beacons.map { it.rotate(ori) })}

    fun reconcile(ogother: Scanner): ResolvedScanner {
        //println("Reconcile ${idx} with ${ogother.idx}")
        for (other in ogother.rotations()) {
            // println(other.beacons[0])
            for (point in other.beacons) {
                val root = findScannerPosition(point, other)
                if (root != null) {
                    // println("Root of ${other.idx} is $root")
                    return ResolvedScanner(Scanner(other.idx, other.beacons.map { root.plus(it) }), root)
                }
            }
        }

        throw Exception("reconcile failed")
    }

    private fun findScannerPosition(point: Point, other: Scanner): Point? {
        // println("$point: $beacons")
        for (target in beacons) {
            val pos = target.subtract(point)
            val projections = other.beacons.map { pos.plus(it) }
            if (projections.intersect(beacons).count() >= 12) {
                return pos
            }
        }

        return null
    }

    private fun pairwiseDistances(): List<Point> {
        val distances = mutableListOf<Point>()
        for (i in 0 until beacons.lastIndex) {
            for (j in i+1..beacons.lastIndex) {
                distances.add(relativeDistance(beacons[i], beacons[j]))
            }
        }

        return distances
    }

    private fun relativeDistance(a: Point, b: Point): Point =
        Point(abs(a.first - b.first), abs(a.second - b.second), abs(a.third - b.third))
}

fun Point.rotate(orientation: Int): Point {
    return when (orientation) {
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
}

fun Point.plus(other: Point): Point =
    Point(first + other.first, second + other.second, third + other.third)

fun Point.subtract(other: Point): Point =
    Point(first - other.first, second - other.second, third - other.third)

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

class ResolvedScanner(val scanner: Scanner, val pos: Point)