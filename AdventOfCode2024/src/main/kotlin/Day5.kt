package day5

import utils.readFileAsLines

fun main() {
    val updates = Updates.fromFile("inputs/day5.txt")
    println("Sum of valid middle page numbers: ${updates.getSumOfValidMiddlePageNumbers()}")
    println("Sum of fixed middle page numbers: ${updates.getFixedMiddlePageNumbers()}")
}

data class Updates(
    val rules: Set<Pair<Int, Int>>,
    val pageNumbers: List<List<Int>>,
) {
    fun getSumOfValidMiddlePageNumbers(): Int {
        return pageNumbers.filter { isValid(it) }.sumOf { getMiddlePageNumber(it) }
    }

    fun getFixedMiddlePageNumbers(): Int {
        return pageNumbers.filterNot { isValid(it) }.map { fixUnorderedPages(it) }.sumOf { getMiddlePageNumber(it) }
    }

    private fun fixUnorderedPages(pages: List<Int>): List<Int> {
        return pages.sortedWith(
            Comparator { a, b ->
                when {
                    Pair(a, b) in rules -> -1
                    Pair(b, a) in rules -> 1
                    else -> 0
                }
            }
        )
    }

    private fun isValid(pages: List<Int>): Boolean {
        val ordered = fixUnorderedPages(pages)
        return ordered == pages
    }

    private fun getMiddlePageNumber(pages: List<Int>): Int {
        require(pages.size % 2 == 1)
        val middleIndex = pages.size / 2
        return pages[middleIndex]
    }

    companion object {
        fun fromFile(fileName: String): Updates {
            val lines = readFileAsLines(fileName)
            val separator = lines.indexOfFirst { it.isEmpty() }
            val section1 = lines.subList(0, separator)
            val section2 = lines.subList(separator + 1, lines.size)
            return Updates(
                rules = section1.map { parseRule(it) }.toSet(),
                pageNumbers = section2.map { parsePageNumbers(it) }
            )
        }

        private fun parseRule(line: String): Pair<Int, Int> {
            val (first, second) = line.split("|")
            return Pair(first.toInt(), second.toInt())
        }

        private fun parsePageNumbers(line: String): List<Int> {
            return line.split(",").map { it.toInt() }
        }
    }
}