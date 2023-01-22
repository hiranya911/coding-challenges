package day11

import utils.readFileAsLines

fun main() {
    val path = "inputs/d11_large.txt"
    sim1(path)
    sim2(path)
}

fun sim1(path: String) {
    val monkeys = parseConfig(path)
    simulate(monkeys, Relief.DivideBy3, rounds = 20)
    println("Monkey business (20 rounds): ${monkeyBusiness(monkeys)}")
}

fun sim2(path: String) {
    val monkeys = parseConfig(path)
    val common = monkeys.map { it.test }.reduce { a, b -> a * b }
    simulate(monkeys, Relief.CommonModulus(common), rounds = 10000)
    println("Monkey business (10000 rounds): ${monkeyBusiness(monkeys)}")
}

fun parseConfig(path: String): List<Monkey> {
    val lines = readFileAsLines(path)
    val buffer = mutableListOf<String>()
    val monkeys = mutableListOf<Monkey>()
    for (line in lines) {
        if (line.isEmpty()) {
            monkeys.add(Monkey.fromLines(buffer))
            buffer.clear()
        } else {
            buffer.add(line)
        }
    }

    if (buffer.isNotEmpty()) {
        monkeys.add(Monkey.fromLines(buffer))
    }
    return monkeys.toList()
}

fun monkeyBusiness(monkeys: List<Monkey>): Long {
    val (a, b) = monkeys.map { it.inspectedItems() }.sortedDescending().take(2)
    return a * b
}

fun simulate(monkeys: List<Monkey>, relief: Relief, rounds: Int) {
    repeat(rounds) {
        monkeys.forEach { it.takeTurn(monkeys, relief) }
    }
}

class Monkey(
    items: List<Int>,
    private val operation: Operation,
    val test: Long,
    private val ifTrue: Int,
    private val ifFalse: Int,
) {
    private val items: ArrayDeque<Long> = ArrayDeque(items.map { it.toLong() })
    private var inspected: Long = 0

    fun inspectedItems(): Long = inspected

    fun takeTurn(monkeys: List<Monkey>, relief: Relief) {
        while (items.isNotEmpty()) {
            var item = items.removeFirst()
            inspected += 1
            item = operation.apply(item)
            item = relief.scaleDown(item)
            if (item % test == 0L) {
                monkeys[ifTrue].throwItem(item)
            } else {
                monkeys[ifFalse].throwItem(item)
            }
        }
    }

    private fun throwItem(item: Long) {
        items.addLast(item)
    }

    companion object {
        fun fromLines(lines: List<String>): Monkey {
            val items = lines[1].trimStart().removePrefix("Starting items: ")
                .split(", ").map { it.toInt() }
            val operation = lines[2].trimStart().removePrefix("Operation: new = old ").let { Operation.fromLine(it) }
            val test = lines[3].trimStart().removePrefix("Test: divisible by ").toLong()
            val ifTrue = lines[4].trimStart().removePrefix("If true: throw to monkey ").toInt()
            val ifFalse = lines[5].trimStart().removePrefix("If false: throw to monkey ").toInt()
            return Monkey(items, operation, test, ifTrue, ifFalse)
        }
    }
}

sealed class Relief {
    abstract fun scaleDown(old: Long): Long

    object DivideBy3 : Relief() {
        override fun scaleDown(old: Long): Long = old / 3
    }

    data class CommonModulus(val mod: Long) : Relief() {
        override fun scaleDown(old: Long): Long = old % mod
    }
}

sealed class Operation {
    abstract fun apply(old: Long): Long

    class Add(private val arg: Int) : Operation() {
        override fun apply(old: Long): Long {
            return old + arg
        }
    }

    class Multiply(private val arg: Int) : Operation() {
        override fun apply(old: Long): Long {
            return old * arg
        }
    }

    object Double : Operation() {
        override fun apply(old: Long): Long {
            return old + old
        }
    }

    object Square : Operation() {
        override fun apply(old: Long): Long {
            return old * old
        }
    }

    companion object {
        fun fromLine(line: String): Operation {
            val (op, arg) = line.split(" ")
            return when (op) {
                "+" -> when (arg) {
                    "old" -> Double
                    else -> Add(arg.toInt())
                }
                "*" -> when (arg) {
                    "old" -> Square
                    else -> Multiply(arg.toInt())
                }
                else -> throw IllegalArgumentException("Invalid operation: $line")
            }
        }
    }
}