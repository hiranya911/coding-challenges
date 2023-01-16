package day5

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val (cargo, moves) = parseCargoInfo("inputs/d5_large.txt")
    println("Top crates: ${computeTopCrates(cargo, moves, batch = false)}")
    println("Top crates (with batching): ${computeTopCrates(cargo, moves, batch = true)}")
}

fun computeTopCrates(cargoConfig: CargoConfig, moves: List<Move>, batch: Boolean): String {
    val cargo = CargoArrangement.fromConfig(cargoConfig)
    moves.forEach { cargo.apply(it, batch) }
    return cargo.topCrates()
}

fun parseCargoInfo(path: String): Pair<CargoConfig, List<Move>> {
    val lines = readFileAsLines(path)
    val cargo = mutableListOf<String>()
    val moves = mutableListOf<String>()
    var phase1 = true
    for (line in lines) {
        if (line.isEmpty()) {
            phase1 = false
            continue
        }

        if (phase1) {
            cargo.add(line)
        } else {
            moves.add(line)
        }
    }

    return CargoConfig(cargo) to moves.map { Move.fromLine(it) }
}

data class CargoConfig(val lines: List<String>)

data class Move(
    val qty: Int,
    val src: Int,
    val dst: Int,
) {
    companion object {
        fun fromLine(line: String): Move {
            val segments = line.split(" ")
            return Move(
                qty = segments[1].toInt(),
                src = segments[3].toInt() - 1,
                dst = segments[5].toInt() - 1,
            )
        }
    }
}

data class Crate(val id: String) {
    override fun toString(): String = "[$id]"
}

typealias CrateStack = Stack<Crate>

class CargoArrangement(private val stacks: List<CrateStack>) {
    companion object {
        fun fromConfig(config: CargoConfig): CargoArrangement {
            val lines = Stack<String>().apply {
                config.lines.dropLast(1).forEach { push(it) }
            }
            val stacks = mutableMapOf<Int, CrateStack>()
            while (lines.isNotEmpty()) {
                val line = lines.pop()
                var crateId: String? = null
                for (idx in 0..line.lastIndex) {
                    val ch = line[idx]
                    if (ch != '[' && ch != ']' && !ch.isWhitespace()) {
                        crateId = "$ch"
                    } else if (ch == ']') {
                        val stackNumber = idx / 4
                        val crate = Crate(crateId!!)
                        val current = stacks.computeIfAbsent(stackNumber) { CrateStack() }
                        current.push(crate)
                        crateId = null
                    }
                }
            }

            return CargoArrangement(stacks.toSortedMap().values.toList())
        }
    }

    fun apply(move: Move, batch: Boolean = false) {
        val src = this.stacks[move.src]
        val dst = this.stacks[move.dst]
        if (batch) {
            val temp = Stack<Crate>();
            for (i in 1..move.qty) {
                temp.push(src.pop())
            }

            while (temp.isNotEmpty()) {
                dst.push(temp.pop())
            }
        } else {
            for (i in 1..move.qty) {
                dst.push(src.pop())
            }
        }
    }

    fun topCrates(): String {
        return stacks.map { it.peek().id }.reduce { a, b -> a + b }
    }

    fun print() {
        val rows = stacks.maxBy { it.size }.size
        val cols = stacks.size

        fun lookup(x: Int, y: Int): Crate? {
            val stack = stacks[x]
            return try {
                stack[y]
            } catch (ex: ArrayIndexOutOfBoundsException) {
                null
            }
        }

        for (y in rows-1 downTo 0) {
            for (x in 0 until cols) {
                val crate = lookup(x, y)?.let { it.toString() } ?: "   "
                print("$crate ")
            }
            print('\n')
        }
    }
}