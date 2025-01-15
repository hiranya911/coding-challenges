package day4

import utils.readFileAsLines

fun main() {
    val puzzle = WordSearch.fromFile("inputs/day4.txt")
    println(puzzle.findXMAS())
    println(puzzle.findXmasCross())
}

data class WordSearch(
    private val characters: Map<Pair<Int, Int>, Char>
) {
    private val width = characters.keys.maxOf { it.first }
    private val height = characters.keys.maxOf { it.second }

    companion object {
        fun fromFile(fileName: String): WordSearch {
            val lines = readFileAsLines(fileName)
            val length = lines.first().lastIndex
            val map = mutableMapOf<Pair<Int, Int>, Char>()
            for (y in 0..lines.lastIndex) {
                for (x in 0..length) {
                    val character = lines[y][x]
                    map[Pair(x, y)] = character
                }
            }

            return WordSearch(map)
        }

        private val NORTH = Pair(0, -1)
        private val NORTH_EAST = Pair(1, -1)
        private val EAST = Pair(1, 0)
        private val SOUTH_EAST = Pair(1, 1)
        private val SOUTH = Pair(0, 1)
        private val SOUTH_WEST = Pair(-1, 1)
        private val WEST = Pair(-1, 0)
        private val NORTH_WEST = Pair(-1, -1)
    }

    fun findXMAS(): Int {
        return characters.keys.sumOf { pos ->
            val words = wordsFrom(pos).map { seq -> seq.map { characters.getValue(it) }.joinToString("") }
            words.count { it == "XMAS" }
        }
    }

    fun findXmasCross(): Int {
        return characters.keys.count { pos ->
            val char = characters.getValue(pos)
            if (char != 'A') return@count false

            val (ul, ur, lr, ll) = listOf(
                pos.plus(NORTH_WEST),
                pos.plus(NORTH_EAST),
                pos.plus(SOUTH_EAST),
                pos.plus(SOUTH_WEST),
            ).takeIf { it.all { (x, y) -> x in 0..width && y in 0..height } } ?: return@count false

            val a = characters.getValue(ul)
            val b = characters.getValue(lr)
            val part1 = (a == 'M' && b == 'S') || (a == 'S' && b == 'M')

            val c = characters.getValue(ur)
            val d = characters.getValue(ll)
            val part2 = (c == 'M' && d == 'S') || (c == 'S' && d == 'M')

            part1 && part2
        }
    }

    private fun wordsFrom(start: Pair<Int, Int>): List<List<Pair<Int, Int>>> {
        return listOfNotNull(
            traverse(start, NORTH, 4),
            traverse(start, NORTH_EAST, 4),
            traverse(start, EAST, 4),
            traverse(start, SOUTH_EAST, 4),
            traverse(start, SOUTH, 4),
            traverse(start, SOUTH_WEST, 4),
            traverse(start, WEST, 4),
            traverse(start, NORTH_WEST, 4),
        )
    }

    private fun traverse(start: Pair<Int, Int>, dir: Pair<Int, Int>, length: Int): List<Pair<Int, Int>>? {
        val result = mutableListOf<Pair<Int, Int>>()
        var current = start
        while (result.size < length) {
            result.add(current)
            current = current.plus(dir)
        }
        return result.takeIf {
            it.all { (x, y) -> x in 0..width && y in 0..height }
        }
    }

    override fun toString(): String {
        return buildString {
            for (y in 0..height) {
                for (x in 0.. width) {
                    append(characters[Pair(x, y)])
                }
                append("\n")
            }
        }
    }
}

private fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> {
    return Pair(first + other.first, second + other.second)
}
