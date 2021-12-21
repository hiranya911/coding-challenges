package day1

fun main() {
    val changes = readFileAsLines("inputs/d1_large.txt").map { it.toInt() }
    val freq = changes.sum()
    println("Frequency = $freq")
    println("First repeated = ${firstRepeatedFrequency(changes)}")
}

fun firstRepeatedFrequency(changes: List<Int>): Int {
    val seen = mutableSetOf<Int>()
    var sum = 0
    while (true) {
        for (change in changes) {
            sum += change
            if (!seen.add(sum)) {
                return sum
            }
        }
    }
}