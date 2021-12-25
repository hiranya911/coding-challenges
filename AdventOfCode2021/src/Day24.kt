package day24

import utils.readFileAsLines

fun main() {
    val analyzer = readFileAsLines("inputs/d24_large.txt").let { ProgramAnalyzer(it) }
    analyzer.search()
}

class ProgramAnalyzer(lines: List<String>) {

    private val a = (0 until 14).map { lines[it * 18 + 5].split(" ").last().toInt() }
    private val b = (0 until 14).map { lines[it * 18 + 4].split(" ").last().toInt() }
    private val c = (0 until 14).map { lines[it * 18 + 15].split(" ").last().toInt() }
    private val limits = (0 until 14).map {
        b.subList(it, b.size).fold(1L) { total, next -> total * next }
    }

    private fun computeResult(stage: Int, w: Int, z: Long): Long  {
        return if (z % 26 + a[stage] == w.toLong()) {
            z / b[stage]
        } else {
            26 * (z / b[stage]) + w + c[stage]
        }
    }

    fun search(stage: Int = 0, z: Long = 0, solution: String = "") {
        if (stage == 14) {
            if (z == 0L) {
                println(solution)
            }

            return
        }

        if (z >= limits[stage]) {
            return
        }

        for (w in 1..9) {
            val newZ = computeResult(stage, w, z)
            search(stage + 1, newZ, solution + w)
        }
    }
}
