package day13

import utils.readFileAsLines

fun main() {
    var paper = FoldingPaper.fromFile("inputs/d13_large.txt")
    paper = paper.foldNext()
    println("Dots after 1st fold = ${paper.dotCount}")

    while (paper.hasFolds) {
        paper = paper.foldNext()
    }

    paper.print()
}

class FoldingPaper(private val dots: Set<Pair<Int, Int>>, private val folds: List<Fold>) {
    private val xlim: Int = dots.maxOf { it.first }
    private val ylim: Int = dots.maxOf { it.second }
    val dotCount: Int = dots.size
    val hasFolds: Boolean = folds.isNotEmpty()

    fun foldNext(): FoldingPaper {
        val fold = folds[0]
        return if (fold.axis == Axis.X) {
            FoldingPaper(xfold(fold.pos), folds.drop(1))
        } else {
            FoldingPaper(yfold(fold.pos), folds.drop(1))
        }
    }

    private fun xfold(pos: Int): Set<Pair<Int, Int>> {
        val (left, right) = dots.partition { it.first <= pos }
        return right.map { Pair(2 * pos - it.first, it.second) }.union(left)
    }

    private fun yfold(pos: Int): Set<Pair<Int, Int>> {
        val (up, down) = dots.partition { it.second <= pos }
        return down.map { Pair(it.first, 2 * pos - it.second) }.union(up)
    }

    fun print() {
        for (y in 0..ylim) {
            for (x in 0..xlim) {
                print(symbol(x, y))
            }

            println()
        }
    }

    private fun symbol(x: Int, y: Int): String =
        if (Pair(x, y) in dots) "#" else "." // we print dots as hashes!

    companion object {
        fun fromFile(fileName: String): FoldingPaper {
            val lines = readFileAsLines(fileName)
            val dots = lines.takeWhile { it != "" }.map { parseCoordinate(it) }.toSet()
            val folds = lines.filter { it.startsWith("fold along") }.map { Fold.fromLine(it) }
            return FoldingPaper(dots, folds)
        }
    }
}

enum class Axis { X, Y }

class Fold(val axis: Axis, val pos: Int) {
    companion object {
        fun fromLine(line: String): Fold =
            line.substringAfter("fold along ").split("=").let {
                Fold(if (it[0] == "x") Axis.X else Axis.Y, it[1].toInt())
            }
    }
}

fun parseCoordinate(line: String): Pair<Int, Int> =
    Pair(line.substringBefore(",").toInt(), line.substringAfter(",").toInt())