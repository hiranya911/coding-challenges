package day18

import utils.readFileAsLines
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    part2()
}

fun part1() {
    val numbers = readFileAsLines("inputs/d18_large.txt").map { SnailNumber.parse(it) }
    var result = numbers[0]
    numbers.drop(1).forEach {
        // println("$result + $it")
        result = result.plus(it)
        result.reduce()
    }

    println(result)
    println(result.magnitude())
}

fun part2() {
    val numbers = readFileAsLines("inputs/d18_large.txt").map { SnailNumber.parse(it) }
    val maxMagnitude = numbers.flatMap { n -> numbers.minus(n).map {
        val result = n.copy().plus(it.copy())
        result.reduce()
        result.magnitude()
    } }.maxOf { it }
    println(maxMagnitude)
}

abstract class SnailNumber(var parent: PairNumber?) {
    abstract fun magnitude(): Int
    abstract fun copy(): SnailNumber

    fun reduce() {
        while (true) {
            val pair = findExplode()
            if (pair != null) {
                pair.explode()
            } else {
                val reg = findSplit()
                if (reg != null) {
                    reg.split()
                } else {
                    break
                }
            }
        }
    }

    fun plus(other: SnailNumber): SnailNumber {
        val result = PairNumber(this, other, null)
        this.parent = result
        other.parent = result
        return result
    }

    fun findExplode(): PairNumber? {
        val frontier = Stack<Node>()
        frontier.push(Node(this, 0))
        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            if (current.num is PairNumber && current.depth == 4) {
                return current.num
            }

            if (current.num is PairNumber) {
                frontier.push(Node(current.num.right, current.depth + 1))
                frontier.push(Node(current.num.left, current.depth + 1))
            }
        }

        return null
    }

    fun findSplit(): RegularNumber? {
        val frontier = Stack<Node>()
        frontier.push(Node(this, 0))
        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            if (current.num is RegularNumber && current.num.num >= 10) {
                return current.num
            }

            if (current.num is PairNumber) {
                frontier.push(Node(current.num.right, current.depth + 1))
                frontier.push(Node(current.num.left, current.depth + 1))
            }
        }

        return null
    }

    fun rightMost(): RegularNumber {
        var current = this
        while (true) {
            if (current is PairNumber) {
                current = current.right
            } else if (current is RegularNumber) {
                return current
            }
        }
    }

    fun leftMost(): RegularNumber {
        var current = this
        while (true) {
            if (current is PairNumber) {
                current = current.left
            } else if (current is RegularNumber) {
                return current
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
                        val pair = PairNumber(left, right, null)
                        left.parent = pair
                        right.parent = pair
                        stack.push(pair)
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

class RegularNumber(var num: Int, parent: PairNumber?): SnailNumber(parent) {
    override fun magnitude(): Int = num

    override fun toString(): String = num.toString()

    override fun copy(): SnailNumber = RegularNumber(num, parent)

    fun split() {
        val left = floor(num.toDouble() / 2).toInt()
        val right = ceil(num.toDouble() / 2).toInt()
        val pair = PairNumber(RegularNumber(left, null), RegularNumber(right, null), this.parent)
        pair.left.parent = pair
        pair.right.parent = pair
        if (this.parent?.left == this) {
            this.parent?.left = pair
        } else {
            this.parent?.right = pair
        }
    }
}

class PairNumber(var left: SnailNumber, var right: SnailNumber, parent: PairNumber?): SnailNumber(parent) {
    override fun magnitude(): Int = 3 * left.magnitude() + 2 * right.magnitude()

    override fun toString(): String = "[${left},${right}]"

    override fun copy(): SnailNumber {
        val leftCopy = left.copy()
        val rightCopy = right.copy()
        val result = PairNumber(leftCopy, rightCopy, parent)
        leftCopy.parent = result
        rightCopy.parent = result
        return result
    }

    fun explode() {
        if (left !is RegularNumber || right !is RegularNumber) {
            throw Exception("left and right must be regular numbers")
        }

        var current = this
        while (current.parent != null) {
            val parent = current.parent!!
            if (parent.left != current) {
                val target = parent.left.rightMost()
                target.num += (this.left as RegularNumber).num
                break
            } else {
                current = parent
            }
        }

        current = this
        while (current.parent != null) {
            val parent = current.parent!!
            if (parent.right != current) {
                val target = parent.right.leftMost()
                target.num += (this.right as RegularNumber).num
                break
            } else {
                current = parent
            }
        }

        val replacement = RegularNumber(0, parent)
        if (parent?.left == this) {
            parent?.left = replacement
        } else {
            parent?.right = replacement
        }
    }
}

class Node(val num: SnailNumber, val depth: Int)
