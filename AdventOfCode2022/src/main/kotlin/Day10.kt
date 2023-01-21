package day10

import utils.readFileAsLines

fun main() {
    val program = readFileAsLines("inputs/d10_large.txt").map { Instruction.fromString(it) }
    val cpu = CPU(program).apply { run() }
    println("Signal strength: ${cpu.signalStrength()}")
    cpu.draw()
}

class Register(private var value: Int) {
    fun get(): Int = value
    fun inc(delta: Int) {
        value += delta
    }
}

class CPU(private val instructions: List<Instruction>) {
    private val x = Register(1)
    private var pc: Int = 0
    private var nextSnapshotAt: Int = 20
    private val signalSnapshots: MutableList<Int> = mutableListOf()
    private val drawnPixels: MutableList<Int> = mutableListOf()

    fun run() {
        var cycles = 0
        while (true) {
            val halt = runCycle(cycles + 1)
            if (halt) break
            cycles += 1
        }
    }

    fun signalStrength(): Int = signalSnapshots.sum()

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

    private fun runCycle(num: Int): Boolean {
        val current = instructions.getOrNull(pc) ?: return true
        if (num == nextSnapshotAt) {
            signalSnapshots.add(x.get() * num)
            nextSnapshotAt += 40
        }

        val crtPosition = (num - 1) % 40
        val sprite = x.get()
        if (crtPosition in sprite - 1..sprite + 1) {
            drawnPixels.add(num - 1)
        }

        if (current.execute(x) == InstructionStatus.Complete) {
            pc += 1
        }
        return false
    }
}

sealed class Instruction() {
    abstract fun execute(x: Register): InstructionStatus

    data class Add(val arg: Int): Instruction() {
        private var cycles: Int = 0

        override fun execute(x: Register): InstructionStatus {
            cycles++
            return when (cycles) {
                1 -> InstructionStatus.Incomplete
                2 -> {
                    x.inc(arg)
                    InstructionStatus.Complete
                }
                else  -> throw IllegalArgumentException("Too many cycles on Add")
            }
        }
    }

    object Noop : Instruction() {
        override fun execute(x: Register): InstructionStatus = InstructionStatus.Complete
    }

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

enum class InstructionStatus { Incomplete, Complete }