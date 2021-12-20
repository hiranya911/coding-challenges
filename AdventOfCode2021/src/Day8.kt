package day8

import utils.pow
import utils.readFileAsLines
import kotlin.math.pow

fun main() {
    val displays = parseDisplays("inputs/d8_large.txt")
    println("Instances of 1, 4, 7, 8 in output = ${displays.sumOf { it.uniqueInstancesInOutput() }}")
    println("Sum of decoded = ${displays.sumOf { it.decode() }}")
}

fun parseDisplays(fileName: String): List<Display> =
    readFileAsLines(fileName).map { Display.fromSting(it) }

class Display(private val inputs: List<String>, private val outputs: List<String>) {

    fun decode(): Int {
        val mapping = deduceMapping()
        val sortedOutput = outputs.map { it.toCharArray().sorted().joinToString("") }
        return (0 until 4).sumOf { mapping.getOrDefault(sortedOutput[it], -1) * (10.pow(3 - it)) }
    }

    fun uniqueInstancesInOutput(): Int =
        outputs.count { it.length in setOf(2, 3, 4, 7) }

    private fun deduceMapping(): Map<String, Int> {
        val temp = inputs.map { it.toCharArray().toSet() }.toMutableList()
        val map = Array<Set<Char>>(10) { setOf() }
        map[1] = temp.findAndRemove { it.size == 2 }
        map[4] = temp.findAndRemove { it.size == 4 }
        map[7] = temp.findAndRemove { it.size == 3 }
        map[8] = temp.findAndRemove { it.size == 7 }

        map[6] = temp.findAndRemove { it.size == 6 && it.intersect(map[1]).size == 1 }
        map[9] = temp.findAndRemove { it.size == 6 && it.intersect(map[4]).size == 4 }
        map[0] = temp.findAndRemove { it.size == 6 }

        map[3] = temp.findAndRemove { it.size == 5 && it.intersect(map[7]).size == 3 }
        map[5] = temp.findAndRemove { it.size == 5 && it.intersect(map[4]).size == 3 }
        map[2] = temp.findAndRemove { true }

        return map.withIndex().associate { (idx, value) -> value.sorted().joinToString("") to idx }
    }

    companion object {
        fun fromSting(line: String): Display =
            Display(
                line.substringBefore(" |").split(" "),
                line.substringAfter("| ").split(" ")
            )
    }
}

fun <T> MutableList<T>.findAndRemove(pred: (s: T) -> Boolean): T =
    first { pred(it) }.also { remove(it) }
