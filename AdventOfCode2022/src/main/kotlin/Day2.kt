package day2

import utils.readFileAsLines

private val INITIAL_MAPPING = mapOf(
    "A" to Shape.Rock,
    "B" to Shape.Paper,
    "C" to Shape.Scissors,
    "X" to Shape.Rock,
    "Y" to Shape.Paper,
    "Z" to Shape.Scissors,
)

fun main() {
    val lines = readFileAsLines("inputs/d2_large.txt")
    val game1 = playGame(lines.map { parseLine(it) })
    println("Outcome with speculative play: $game1")

    val game2 = playGame(lines.map { parseLineCorrectly(it) })
    println("Outcome with correct play: $game2")
}

fun parseLine(line: String): Pair<Shape, Shape> {
    val (s1, s2) = line.split(" ")
    return INITIAL_MAPPING.getValue(s1) to INITIAL_MAPPING.getValue(s2)
}

fun parseLineCorrectly(line: String): Pair<Shape, Shape> {
    val (s1, s2) = line.split(" ")
    val shape1 = INITIAL_MAPPING.getValue(s1)
    val shape2 = when (s2) {
         "X" -> Shape.getRule(shape1).first
         "Y" -> shape1
         "Z" -> Shape.getRule(shape1).second
         else -> throw IllegalArgumentException("Invalid symbol $s2")
    }
    return shape1 to shape2
}

fun playGame(rounds: List<Pair<Shape, Shape>>): Score = rounds
    .map { computeScore(it.first, it.second) }
    .reduce { s1, s2 -> s1.plus(s2) }

fun computeScore(player1: Shape, player2: Shape): Score {
    var score1 = player1.score
    var score2 = player2.score
    val result = Shape.compare(player1, player2)
    if (result > 0) {
        score1 += 6
    } else if (result < 0) {
        score2 += 6
    } else {
        score1 += 3
        score2 += 3
    }

    return Score(score1, score2)
}

sealed class Shape(val score: Int) {
    object Rock : Shape(1)
    object Paper : Shape(2)
    object Scissors : Shape(3)

    companion object {
        fun getRule(shape: Shape): Pair<Shape, Shape> {
            return when (shape) {
                is Rock -> Scissors to Paper
                is Paper -> Rock to Scissors
                is Scissors -> Paper to Rock
            }
        }

        fun compare(shape1: Shape, shape2: Shape): Int {
            val rule = getRule(shape1)
            return if (rule.first == shape2) {
                1
            } else if (rule.second == shape2) {
                -1
            } else {
                0
            }
        }
    }
}

data class Score(val player1: Int, val player2: Int) {
    fun plus(other: Score): Score = Score(this.player1 + other.player1, this.player2 + other.player2)
}