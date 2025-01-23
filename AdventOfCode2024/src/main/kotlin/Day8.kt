package day8

import utils.readFileAsLines

fun main() {
    val locations = Locations.fromFile("inputs/day8.txt")
    println("Antinodes: ${locations.antinodes().size}")
    println("Antinodes with harmonics: ${locations.antinodes(harmonics = true).size}")
}

data class Locations(
    val map: Map<Pair<Int, Int>, Char>
) {
    companion object {
        fun fromFile(fileName: String): Locations {
            val lines = readFileAsLines(fileName)
            val map = mutableMapOf<Pair<Int, Int>, Char>()
            for (y in lines.indices) {
                val line = lines[y]
                for (x in line.indices) {
                    map[Pair(x, y)] = line[x]
                }
            }
            return Locations(map)
        }
    }

    fun antinodes(harmonics: Boolean = false): Set<Pair<Int, Int>> {
        val grouped = map.entries.map { it.value to it.key }.groupBy({ it.first }, { it.second }).minus('.')
        return grouped.values.flatMap { antinodes(it, harmonics) }.toSet()
    }

    private fun antinodes(positions: List<Pair<Int, Int>>, harmonics: Boolean): Set<Pair<Int, Int>> {
        val result = mutableSetOf<Pair<Int, Int>>()
        for (i in 0..<positions.lastIndex) {
            val a = positions[i]
            for (j in i+1..positions.lastIndex) {
                val b = positions[j]
                if (harmonics) {
                    result.addAll(antinodesWithHarmonics(a, b))
                } else {
                    result.addAll(antinodes(a, b))
                }
            }
        }

        return result
    }

    private fun antinodes(a: Pair<Int, Int>, b: Pair<Int, Int>): Set<Pair<Int, Int>> {
        val dy = b.second - a.second
        val dx = b.first - a.first
        return listOfNotNull(
            Pair(b.first + dx, b.second + dy),
            Pair(a.first - dx, a.second - dy),
        ).filter { it in map }.toSet()
    }

    private fun antinodesWithHarmonics(a: Pair<Int, Int>, b: Pair<Int, Int>): Set<Pair<Int, Int>> {
        val result = mutableSetOf<Pair<Int, Int>>()
        val dy = b.second - a.second
        val dx = b.first - a.first

        var dir1 = b
        while (dir1 in map) {
            result.add(dir1)
            dir1 = Pair(dir1.first + dx, dir1.second + dy)
        }

        var dir2 = a
        while (dir2 in map) {
            result.add(dir2)
            dir2 = Pair(dir2.first - dx, dir2.second - dy)
        }

        return result
    }
}