package day3

import utils.readFileAsLines

private val MUL_REGEX = """(mul\(\d{1,3},\d{1,3}\))|(do\(\))|(don't\(\))""".toRegex()

fun main() {
    val memory = readFileAsLines("inputs/day3.txt").joinToString()
    val instructions = MUL_REGEX.findAll(memory).map { Instruction.fromString(it.value) }.toList()
    println(runProgram(instructions, handleConditionals = false))
    println(runProgram(instructions, handleConditionals = true))
}

private fun runProgram(prog: List<Instruction>, handleConditionals: Boolean): Int {
    var enabled = true
    var sum = 0
    for (inst in prog) {
        when (inst) {
            is Instruction.Mul -> {
                if (enabled) sum += inst.a * inst.b
            }
            is Instruction.Do -> {
                if (handleConditionals) enabled = true
            }
            is Instruction.Dont -> {
                if (handleConditionals) enabled = false
            }
        }
    }
    return sum
}

sealed class Instruction {
    data class Mul(val a: Int, val b: Int) : Instruction()
    object Do : Instruction()
    object Dont : Instruction()

    companion object {
        fun fromString(str: String): Instruction {
            val op = str.substringBefore("(")
            return when (op) {
                "mul" -> {
                    val args = str.removePrefix("mul(").removeSuffix(")").split(",").map { it.toInt() }
                    require(args.size == 2)
                    Mul(args[0], args[1])
                }
                "do" -> Do
                "don't" -> Dont
                else -> throw IllegalArgumentException("Unknown operation: $op")
            }
        }
    }
}