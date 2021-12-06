package day1

import utils.readFileAsLines

fun main() {
    val sonar = readSonarReport("inputs/d1_large.txt")
    println("Increments = ${countIncrements(sonar)}")
    println("Increments (w3) = ${countIncrements(sonar, 3)}")
}

fun countIncrements(sonar: Array<Int>, windowSize: Int = 1): Int {
    var counter = 0
    val window1 = SlidingWindow(sonar, 0, windowSize)
    val window2 = SlidingWindow(sonar, 1, windowSize)
    while (window2.valid()) {
        if (window2.sum() > window1.sum()) {
            counter++
        }

        window2.slide()
        window1.slide()
    }

    return counter
}

class SlidingWindow(private val sonar: Array<Int>, private var start: Int, private val size: Int) {

    private val maxStart: Int = sonar.size - size

    fun sum(): Int = sonar.slice(start until start+size).sum()

    fun slide() {
        start++
    }

    fun valid(): Boolean = start <= maxStart
}

fun readSonarReport(fileName: String): Array<Int> =
    readFileAsLines(fileName).map { it.toInt() }.toTypedArray()
