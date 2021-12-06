package day2

import utils.readFileAsLines
import java.lang.Exception

fun main() {
    val instructions = readPilotInstructions("inputs/d2_large.txt")

    val nav = Navigator().apply {
        pilot(instructions)
    }
    println("Coordinate = ${nav.coordinate()}")

    val aimedNav = Navigator(PositionWithAim()).apply {
        pilot(instructions)
    }
    println("Aimed Coordinate = ${aimedNav.coordinate()}")
}

fun readPilotInstructions(fileName: String): List<Instruction> =
    readFileAsLines(fileName).map { Instruction.parse(it) }.toList()

class Navigator(private var position: Position = Position()) {

    fun pilot(instructions: List<Instruction>) {
        instructions.forEach {
            position = position.update(it)
        }
    }

    fun coordinate(): Int = position.horizontal * position.depth
}

open class Position(val horizontal: Int = 0, val depth: Int = 0) {
    open fun update(inst: Instruction): Position =
        when (inst.direction) {
            "forward" -> Position(horizontal + inst.units, depth)
            "down" -> Position(horizontal, depth + inst.units)
            "up" -> Position(horizontal, depth - inst.units)
            else -> throw Exception("unknown direction ${inst.direction}")
        }
}

class PositionWithAim(horizontal: Int = 0, depth: Int = 0, private val aim: Int = 0):
    Position(horizontal, depth) {
    override fun update(inst: Instruction): Position =
        when (inst.direction) {
            "forward" -> PositionWithAim(horizontal + inst.units, depth + aim * inst.units, aim)
            "down" -> PositionWithAim(horizontal, depth, this.aim + inst.units)
            "up" -> PositionWithAim(horizontal, depth, this.aim - inst.units)
            else -> throw Exception("unknown direction ${inst.direction}")
        }
}

data class Instruction(val direction: String, val units: Int) {
    companion object {
        fun parse(line: String): Instruction =
            Instruction(line.substringBefore(" "), line.substringAfter(" ").toInt())
    }
}