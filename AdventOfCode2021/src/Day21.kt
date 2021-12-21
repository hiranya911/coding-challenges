package day21

import java.util.*
import kotlin.math.min

fun main() {
    // var game = State(3, 7)
    var game = State(9, 8)
    val dice = DeterministicDice()
    while (!game.isDone()) {
        game = game.takeTurn((1..3).sumOf { dice.next() })
    }

    println("Number from deterministic die = ${game.signature()}")

    val init = State(9, 8)
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
            quantumRolls.map { current.takeTurn(it.key).also { s -> s.universes = current.universes * it.value } }
                .forEach { frontier.push(it) }
        }
    }

    println("$p1wins, $p2wins")
}

class State(
    val player1: Int,
    val player2: Int,
    val score1: Int = 0,
    val score2: Int = 0,
    val rolls: Long = 0,
    val p1Turn: Boolean = true,
) {
    var universes: Long = 1

    fun takeTurn(sum: Int, r: Int = 3): State =
        if (p1Turn) {
            val pos = (player1 + sum) % 10
            State(pos, player2, score1 + pos + 1, score2, rolls + r,false)
        } else {
            val pos = (player2 + sum) % 10
            State(player1, pos, score1, score2 + pos + 1, rolls + r, true)
        }

    fun isDone(winningScore: Int = 1000): Boolean =
        score1 >= winningScore || score2 >= winningScore

    fun signature(): Long = min(score1, score2) * rolls
}

interface Dice {
    fun next(): Int
}

class DeterministicDice(val sides: Int = 100): Dice {

    var start = 1

    override fun next(): Int {
        val result = start
        start++
        if (start > sides) {
            start = 1
        }

        return result
    }
}

val quantumRolls = mapOf(
    3 to 1,
    4 to 3,
    5 to 6,
    6 to 7,
    7 to 6,
    8 to 3,
    9 to 1,
)