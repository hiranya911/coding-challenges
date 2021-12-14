package day14

import utils.readFileAsLines

fun main() {
    val lines = readFileAsLines("inputs/d14_large.txt")
    val poly = Polymerization.fromLines(lines.subList(2, lines.size))
    println("After 10 steps = ${poly.process(lines[0], 10)}")
    println("After 40 steps = ${poly.process(lines[0], 40)}")
}

class Polymerization(private val rules: Map<String, PairInsertion>) {

    fun process(input: String, steps: Int): Long {
        var pairs = input.zipWithNext().map { "${it.first}${it.second}" }
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toLong() }
        for (i in 1..steps) {
            pairs = pairs.flatMap { expand(it.key, it.value) }
                .groupingBy { it.first }
                .fold(0L) { total, next -> total + next.second }
        }

        val counts = pairs.map { it.key[1] to it.value }
            .plusElement(Pair(input[0], 1L)) // Add the first letter to complete the count
            .groupingBy { it.first }
            .fold(0L) { total, next -> total + next.second }
        return counts.maxOf { it.value } - counts.minOf { it.value }
    }

    private fun expand(pair: String, count: Long): List<Pair<String, Long>> =
        (rules[pair]?.expand()?.map { it to count }) ?: listOf(pair to count)

    companion object {
        fun fromLines(lines: List<String>): Polymerization =
            lines.map { PairInsertion.fromString(it) }
                .associateBy { it.input }
                .let { Polymerization(it) }
    }
}

class PairInsertion(val input: String, private val output: Char) {

    fun expand(): List<String> = listOf("${input[0]}$output", "$output${input[1]}")

    companion object {
        fun fromString(line: String): PairInsertion =
            PairInsertion(line.substringBefore(" ->"), line.substringAfter("-> ")[0])
    }
}