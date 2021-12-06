package day4

import utils.readFileAsLines

fun main() {
    val bingo = Bingo.fromFile("inputs/d4_large.txt")
    bingo.play()
}

class Bingo(private val numbers: List<Int>, private val boards: List<Board>) {
    fun play() {
        val winners = mutableSetOf<Board>()
        for (num in numbers) {
            println("Placing $num...")
            for ((idx, board) in boards.withIndex()) {
                if (winners.contains(board)) {
                    continue
                }

                if (board.mark(num)) {
                    println("  Board ${idx + 1}; Score = ${num * board.sumOfUnmarked()}")
                    winners.add(board)
                    if (winners.size == boards.size) {
                        return
                    }
                }
            }
        }
    }

    fun print() {
        println(numbers)
        boards.forEach {
            println()
            it.print()
        }
    }

    companion object {
        fun fromFile(fileName: String): Bingo {
            val lines = readFileAsLines(fileName)
            val numbers = parseLine(lines[0], delimiters = ",")
            val boards = lines.subList(2, lines.size).splitBySeparator()
                .map { Board.fromLines(it) }
            return Bingo(numbers, boards)
        }
    }
}

fun List<String>.splitBySeparator(sep: String = ""): List<List<String>> {
    val result = mutableListOf<List<String>>()
    var buffer = mutableListOf<String>()
    forEach {
        if (it == "") {
            if (buffer.size > 0) {
                result.add(buffer)
                buffer = mutableListOf()
            }
        } else {
            buffer.add(it)
        }
    }

    if (buffer.size > 0) {
        result.add(buffer)
    }

    return result
}

fun parseLine(line: String, delimiters: String = " "): List<Int> =
    line.split(delimiters).filter { it != "" }.map { it.toInt() }

class Board(private val rows: Int, private val cols: Int, private val cellToNumber: Map<Pair<Int, Int>, Int>) {

    private val numberToCells = cellToNumber.keys.groupBy { k -> cellToNumber[k] }
    private val marked = mutableSetOf<Pair<Int, Int>>()

    fun mark(num: Int): Boolean {
        val cells = numberToCells[num] ?: listOf()
        marked.addAll(cells)
        return cells.any { checkBingoAt(it) }
    }

    fun sumOfUnmarked(): Int =
        cellToNumber.keys.filterNot { marked.contains(it) }.sumOf { cellToNumber[it] ?: 0 }

    private fun checkBingoAt(cell: Pair<Int, Int>): Boolean =
        (0 until rows).all { marked.contains(Pair(cell.first, it))}
                || (0 until cols).all { marked.contains(Pair(it, cell.second))}

    fun print() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                print("${cellToNumber[Pair(col, row)]} ")
            }

            println()
        }
    }

    companion object {
        fun fromLines(lines: List<String>): Board {
            val rows = lines.map { parseLine(it) }
            val map = rows.flatMapIndexed { ridx, row ->
                row.mapIndexed { cidx, col -> Pair(cidx, ridx) to col }
            }.toMap()
            return Board(rows.size, rows[0].size, map.toMap())
        }
    }
}