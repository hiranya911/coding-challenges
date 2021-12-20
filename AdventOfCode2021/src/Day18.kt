package day18

import utils.readFileAsLines
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    val numbers = readFileAsLines("inputs/d18_large.txt").map { SnailNumber.parse(it) }
    val result = numbers.map { it.copy() }.reduce { acc, next -> acc.plus(next) }
    println("Magnitude of sum = ${result.magnitude()}")

    val maxMagnitude = numbers
        .flatMap { n -> numbers.minus(n).map { n.copy().plus(it.copy()).magnitude() } }
        .maxOf { it }
    println("Largest magnitude = $maxMagnitude")
}

sealed class SnailNumber(var parent: PairNumber?) {
    abstract fun magnitude(): Int
    abstract fun copy(): SnailNumber

    fun plus(other: SnailNumber): SnailNumber =
        PairNumber(this, other, null).reduce()

    fun leftMost(): RegularNumber {
        var current: SnailNumber = this
        while (true) {
            when (current) {
                is PairNumber -> current = current.left
                is RegularNumber -> return current
            }
        }
    }

    fun rightMost(): RegularNumber {
        var current = this
        while (true) {
            when (current) {
                is PairNumber -> current = current.right
                is RegularNumber -> return current
            }
        }
    }

    companion object {
        fun parse(s: String): SnailNumber {
            val stack = Stack<SnailNumber>()
            s.forEach {
                when {
                    it.isDigit() -> stack.push(RegularNumber(it.digitToInt(), null))
                    it == ']' -> {
                        val right = stack.pop()
                        val left = stack.pop()
                        stack.push(PairNumber(left, right, null))
                    }
                }
            }

            if (stack.size != 1) {
                throw Exception("parse error: ${stack.size}")
            }

            return stack.pop()
        }
    }
}

class RegularNumber(var value: Int, parent: PairNumber?): SnailNumber(parent) {

    override fun magnitude(): Int = value

    override fun toString(): String = value.toString()

    override fun copy(): SnailNumber = RegularNumber(value, parent)

    fun split() {
        val left = floor(value.toDouble() / 2).toInt()
        val right = ceil(value.toDouble() / 2).toInt()
        val pair = PairNumber(RegularNumber(left, null), RegularNumber(right, null), this.parent)
        if (this.parent!!.left == this) {
            this.parent!!.left = pair
        } else {
            this.parent!!.right = pair
        }
    }
}

class PairNumber(var left: SnailNumber, var right: SnailNumber, parent: PairNumber?): SnailNumber(parent) {

    init {
        left.parent = this
        right.parent = this
    }

    override fun magnitude(): Int = 3 * left.magnitude() + 2 * right.magnitude()

    override fun toString(): String = "[${left},${right}]"

    override fun copy(): SnailNumber = PairNumber(left.copy(), right.copy(), parent)

    fun reduce(): PairNumber {
        while (true) {
            val pair = findLeftMost<PairNumber>{ node -> node.num is PairNumber && node.depth == 4 }
            if (pair != null) {
                pair.explode()
            } else {
                val reg = findLeftMost<RegularNumber>{ node -> node.num is RegularNumber && node.num.value >= 10 }
                if (reg != null) {
                    reg.split()
                } else {
                    break
                }
            }
        }

        return this
    }

    private fun mergeLeft(source: RegularNumber) {
        var current = this
        while (current.parent != null) {
            val parent = current.parent!!
            if (parent.left != current) {
                val target = parent.left.rightMost()
                target.value += source.value
                return
            } else {
                current = parent
            }
        }

    }

    private fun mergeRight(source: RegularNumber) {
        var current = this
        while (current.parent != null) {
            val parent = current.parent!!
            if (parent.right != current) {
                val target = parent.right.leftMost()
                target.value += source.value
                return
            } else {
                current = parent
            }
        }
    }

    private fun explode() {
        if (left !is RegularNumber || right !is RegularNumber) {
            throw Exception("left and right must be regular numbers")
        }

        mergeLeft(this.left as RegularNumber)
        mergeRight(this.right as RegularNumber)
        val replacement = RegularNumber(0, parent)
        if (parent?.left == this) {
            parent?.left = replacement
        } else {
            parent?.right = replacement
        }
    }

    private fun <T : SnailNumber> findLeftMost(goal: (Node) -> Boolean): T? {
        val frontier = Stack<Node>()
        frontier.push(Node(this, 0))
        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            if (goal(current)) {
                return current.num as T
            }

            if (current.num is PairNumber) {
                frontier.push(Node(current.num.right, current.depth + 1))
                frontier.push(Node(current.num.left, current.depth + 1))
            }
        }

        return null
    }
}

class Node(val num: SnailNumber, val depth: Int)
