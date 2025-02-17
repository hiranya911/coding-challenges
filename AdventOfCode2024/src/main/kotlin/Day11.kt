package day11

private const val INPUT_SMALL = "125 17"
private const val INPUT = "4022724 951333 0 21633 5857 97 702 6"

fun main() {
    val stones = parseInput(INPUT)
    val after25 = stones.simulate(25)
    println("Stones after 25 rounds: $after25")

    val after75 = stones.simulate(75)
    println("Stones after 75 rounds: $after75")
}

private fun parseInput(input: String): List<Long> {
    return input.split(" ").map { it.toLong() }
}

fun List<Long>.simulate(blinks: Int): Long {
    val cache = mutableMapOf<Pair<Long, Int>, Long>()

    fun compute(stone: Long, blink: Int): Long {
        if (blink == 0) return 1
        val key = (stone to blink)
        if (key in cache) {
            return cache.getValue(key)
        }

        val text = stone.toString()
        val size = when {
            stone == 0L -> compute(1, blink - 1)
            text.length % 2 == 0 -> {
                val mid = text.length / 2
                val left = text.substring(0, mid).toLong()
                val right = text.substring(mid).toLong()
                compute(left, blink - 1) + compute(right, blink - 1)
            }
            else -> compute(stone * 2024, blink - 1)
        }

        cache[key] = size
        return size
    }

    return sumOf { compute(it, blinks) }
}
