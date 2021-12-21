package day21

import java.util.*
import kotlin.math.min

fun main() {
    val game = State.withStartingPositions(10, 9)
    println("Number from deterministic die = ${playWithDeterministicDice(game)}")
    println("Most quantum wins = ${findMostQuantumWins(game)}")
}

fun playWithDeterministicDice(init: State): Long {
    var game = init
    var num = 0
    val dice = generateSequence { ((num++) % 100) + 1 }.iterator()
    while (!game.isDone()) {
        game = game.takeTurn((1..3).sumOf { dice.next() })
    }

    return game.signature()
}

fun findMostQuantumWins(init: State): Long {
    val frontier = Stack<State>()
    frontier.push(init)
    var p1wins = 0L
    var p2wins = 0L
    while (frontier.isNotEmpty()) {
        val current = frontier.pop()
        if (current.isDone(21)) {
            if (current.score1 > current.score2) {
                p1wins += current.universes
            } else {
                p2wins += current.universes
            }
        } else {
            quantumRolls.map { current.takeTurn(it.key, it.value) }.forEach { frontier.push(it) }
        }
    }

    return if (p1wins > p2wins) p1wins else p2wins
}

class State(
    private val player1: Int,
    private val player2: Int,
    val score1: Int = 0,
    val score2: Int = 0,
    private val rolls: Long = 0,
    private val p1Turn: Boolean = true,
    val universes: Long = 1,
) {
    fun takeTurn(sum: Int, unis: Int = 1): State =
        if (p1Turn) {
            val pos = (player1 + sum) % 10
            State(pos, player2, score1 + pos + 1, score2, rolls + 3,false, universes * unis)
        } else {
            val pos = (player2 + sum) % 10
            State(player1, pos, score1, score2 + pos + 1, rolls + 3, true, universes * unis)
        }

    fun isDone(winningScore: Int = 1000): Boolean =
        score1 >= winningScore || score2 >= winningScore

    fun signature(): Long = min(score1, score2) * rolls

    companion object {
        fun withStartingPositions(player1: Int, player2: Int): State =
            State(player1-1, player2-1)
    }
}

val quantumRolls = mapOf(3 to 1, 4 to 3, 5 to 6, 6 to 7, 7 to 6, 8 to 3, 9 to 1)