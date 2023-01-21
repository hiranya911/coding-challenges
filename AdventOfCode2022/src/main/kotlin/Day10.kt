package day10

import utils.readFileAsLines

fun main() {
    val program = readFileAsLines("inputs/d10_large.txt").map { Instruction.fromString(it) }
    val cpu = CPU().apply { program.forEach { execute(it) } }
    println("Signal strength: ${cpu.signalStrength()}")
    cpu.draw()
}

class CPU {
    private var x: Int = 1

    /**
     * Number of cycles completed.
     */
    private var cycles: Int = 0
    private var nextSnapshotAt: Int = 20
    private val signalStrengths = mutableListOf<Int>()
    private val drawnPixels = mutableListOf<Int>()

    fun signalStrength(): Int = signalStrengths.sum()

    fun execute(inst: Instruction) {
        when (inst) {
            is Instruction.Noop -> tick()
            is Instruction.Add -> {
                tick()
                tick()
                x += inst.arg
            }
        }
    }

    fun draw() {
        for (i in 1 .. 240) {
            if (i-1 in drawnPixels) {
                print("##")
            } else {
                print("..")
            }

            if (i % 40 == 0) println()
        }
    }

    private fun tick() {
        val crtPosition = cycles % 40
        if (crtPosition in x-1..x+1) {
            drawnPixels.add(cycles)
        }

        val current = cycles + 1
        if (current == nextSnapshotAt) {
            signalStrengths.add(x * current)
            nextSnapshotAt += 40
        }

        cycles++
    }
}

sealed class Instruction() {
    data class Add(val arg: Int): Instruction()
    object Noop : Instruction()

    companion object {
        fun fromString(str: String): Instruction {
            return if (str == "noop") {
                return Noop
            } else if (str.startsWith("addx ")) {
                return Add(str.split(" ")[1].toInt())
            } else {
                throw IllegalArgumentException("Invalid instruction: $str")
            }
        }
    }
}