package day13

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val packets = readFileAsLines("inputs/d13_large.txt").filter { it.isNotEmpty() }.map { parse(it) }
    println("In order packets = ${inOrderPacketPairs(packets)}")
    println("Decoder key = ${decodePackets(packets)}")
}

fun inOrderPacketPairs(packets: List<Packet>): Int {
    val pairs = (0..packets.lastIndex step 2).map { packets[it] to packets[it + 1] }
    return pairs.withIndex().mapNotNull { (idx, pair) -> if (pair.first < pair.second) idx + 1 else null }.sum()
}

fun decodePackets(packets: List<Packet>): Int {
    val divider1 = parse("[[2]]")
    val divider2 = parse("[[6]]")
    val ordered = (packets + listOf(divider1, divider2)).sorted()
    return (ordered.indexOf(divider1) + 1) * (ordered.indexOf(divider2) + 1)
}

sealed class Packet : Comparable<Packet> {
    override fun compareTo(other: Packet): Int {
        if (this is Integer && other is Integer) {
            return this.number - other.number
        }

        if (this is Sequence && other is Sequence) {
            for (idx in 0..this.list.lastIndex) {
                if (idx > other.list.lastIndex) return this.list.size - other.list.size
                val leftChild = this.list[idx]
                val rightChild = other.list[idx]
                val childComparison = leftChild.compareTo(rightChild)
                if (childComparison != 0) return childComparison
            }

            return this.list.size - other.list.size
        }

        val leftAsList = if (this is Sequence) this else Sequence(mutableListOf(this))
        val rightAsList = if (other is Sequence) other else Sequence(mutableListOf(other))
        return leftAsList.compareTo(rightAsList)
    }

    data class Integer(val number: Int) : Packet() {
        override fun toString(): String {
            return number.toString()
        }
    }

    data class Sequence(val list: MutableList<Packet>) : Packet() {
        override fun toString(): String {
            return list.toString()
        }
    }
}

fun parse(str: String): Packet {
    val stack = Stack<Packet>()
    stack.push(Packet.Sequence(mutableListOf()))

    var idx = 0
    while (idx < str.length) {
        when (str[idx]) {
            '[' -> {
                stack.push(Packet.Sequence(mutableListOf()))
                idx++
            }
            ']' -> {
                val top = stack.pop()
                (stack.peek() as Packet.Sequence).list.add(top)
                idx++
            }
            ',' -> {
                idx++
            }
            else -> {
                val number = StringBuilder()
                while (str[idx].isDigit()) {
                    number.append(str[idx])
                    idx++
                }
                (stack.peek() as Packet.Sequence).list.add(Packet.Integer(number.toString().toInt()))
            }
        }
    }

    return (stack.pop() as Packet.Sequence).list.first()
}
