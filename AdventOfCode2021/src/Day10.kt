package day10

import utils.readFileAsLines
import java.util.*

fun main() {
    val lines = readFileAsLines("inputs/d10_large.txt").map { NavSyntax(it) }
    println("Syntax error score = ${lines.sumOf { it.syntaxErrorScore }}")

    val autoCompleteScores = lines
        .filter { it.syntaxErrorScore == 0 }
        .map { it.autoCompleteScore }
        .sorted()
    println("Auto complete score = ${autoCompleteScores[autoCompleteScores.size / 2]}")
}

class NavSyntax(line: String) {

    private val stack = Stack<Char>()

    val syntaxErrorScore: Int = parseString(line)
    val autoCompleteScore: Long =
        stack.reversed().map {
            when (it) {
                '(' -> 1
                '[' -> 2
                '{' -> 3
                else -> 4
            }
        }.fold(0) { total, next -> 5 * total + next }

    private fun parseString(line: String): Int {
        for (ch in line) {
            when (ch) {
                '(', '[', '{', '<' -> stack.push(ch)
                ')' -> if (stack.pop() != '(') return 3
                ']' -> if (stack.pop() != '[') return 57
                '}' -> if (stack.pop() != '{') return 1197
                '>' -> if (stack.pop() != '<') return 25137
            }
        }

        return 0
    }
}