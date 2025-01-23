package day7

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val equations = readFileAsLines("inputs/day7.txt").map { Equation.fromString(it) }
    println("Calibration result: ${computeCalibrationResult(equations)}")
    println("Calibration result: ${computeCalibrationResult(equations, includeConcat = true)}")
}

fun computeCalibrationResult(equations: List<Equation>, includeConcat: Boolean = false): Long {
    return equations.sumOf {
        val solution = it.solve(includeConcat)
        if (solution != null) {
            it.result
        } else 0L
    }
}

data class Equation(val result: Long, val args: List<Long>) {
    companion object {
        fun fromString(str: String): Equation {
            val prefix = str.substringBefore(": ")
            val suffix = str.substringAfter(": ")
            return Equation(
                result = prefix.toLong(),
                args = suffix.split(" ").map { it.toLong() }
            )
        }
    }

    fun solve(includeConcat: Boolean): Node? {
        val root = Node(index = 0, value = args.first())
        val frontier = Stack<Node>()
        frontier.push(root)
        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            val value = current.value
            if (current.index == args.lastIndex && value == result) return current
            if (value > result) continue

            val children = successors(current, includeConcat)
            children.forEach { frontier.push(it) }
        }

        return null
    }

    private fun successors(node: Node, includeConcat: Boolean): List<Node> {
        val nextIndex = node.index + 1
        if (nextIndex > args.lastIndex) return emptyList()
        val operators = if (includeConcat) {
            listOf(Operator.ADD, Operator.MULTIPLY, Operator.CONCAT)
        } else {
            listOf(Operator.ADD, Operator.MULTIPLY)
        }

        val nextNum = args[nextIndex]
        return operators.map {
            val newNum = when (it) {
                Operator.ADD -> node.value + nextNum
                Operator.MULTIPLY -> node.value * nextNum
                Operator.CONCAT -> "${node.value}$nextNum".toLong()
            }
            Node(index = nextIndex, value = newNum)

        }
    }
}

enum class Operator {
    ADD, MULTIPLY, CONCAT
}

data class Node(
    val index: Int,
    val value: Long,
)
