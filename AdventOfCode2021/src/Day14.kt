package day14

import utils.readFileAsLines

fun main() {
    val lines = readFileAsLines("inputs/d14_large.txt")
    val poly = Polymerization.fromLines(lines.subList(2, lines.size))
    println("After 10 steps = ${poly.process(lines[0], 10)}")
    println("After 40 steps = ${poly.process(lines[0], 40)}")
}

class Polymerization(private val rules: Map<Pair<Char, Char>, PairInsertion>) {

    fun process(input: String, steps: Int): Long {
        val counts = input.groupingBy { it }.eachCount().mapValues { it.value.toLong() }.toMutableMap()
        var pairs = input.zipWithNext().groupingBy { it }.eachCount().mapValues { it.value.toLong() }
        for (i in 1..steps) {
            val newPairs = mutableMapOf<Pair<Char, Char>, Long>()
            pairs.forEach {
                val rule = rules[it.key]
                if (rule != null) {
                    counts[rule.output] = (counts[rule.output] ?: 0) + it.value
                    rule.expand().forEach { p -> newPairs[p] = (newPairs[p] ?: 0) + it.value }
                } else {
                    newPairs[it.key] = (newPairs[it.key] ?: 0) + it.value
                }
            }
            pairs = newPairs
        }

        return counts.maxOf { it.value } - counts.minOf { it.value }
    }

    companion object {
        fun fromLines(lines: List<String>): Polymerization =
            lines.map { PairInsertion.fromString(it) }.associateBy { Pair(it.input[0], it.input[1]) }
                .let { Polymerization(it) }
    }
}

class PairInsertion(val input: String, val output: Char) {

    fun expand(): List<Pair<Char, Char>> =
        listOf(
            Pair(input[0], output),
            Pair(output, input[1])
        )

    companion object {
        fun fromString(line: String): PairInsertion =
            PairInsertion(line.substringBefore(" ->"), line.substringAfter("-> ")[0])
    }
}