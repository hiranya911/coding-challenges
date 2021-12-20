package day20

import utils.pow
import utils.plus
import utils.readFileAsLines

fun main() {
    var image = Image.fromFile("inputs/d20_large.txt")
    for (i in 1..50) {
        image = image.enhance()
        if (i == 2) {
            println("Active pixels after 2 rounds = ${image.activePixels()}")
        }
    }

    println("Active pixels after 50 rounds = ${image.activePixels()}")
}

class Image(
    private val pixels: Map<Pair<Int, Int>, Int>,
    private val enhancer: List<Int>,
    private val defaultPixel: Int = 0,
    xBounds: Pair<Int, Int>? = null,
    yBounds: Pair<Int, Int>? = null) {

    private val minX = xBounds?.first ?: pixels.keys.minOf { it.first }
    private val maxX = xBounds?.second ?: pixels.keys.maxOf { it.first }
    private val minY = yBounds?.first ?: pixels.keys.minOf { it.second }
    private val maxY = yBounds?.second ?: pixels.keys.maxOf { it.second }

    fun activePixels(): Int = pixels.values.sumOf { it }

    fun enhance(): Image {
        val result = pixels.keys.associateWith { getEnhancedPixel(it) }.toMutableMap()
        enhanceHorizontalEdges(result)
        enhanceVerticalEdges(result)
        return Image(result, enhancer, nextDefaultPixel(), Pair(minX-1, maxX+1), Pair(minY-1, maxY+1))
    }

    private fun enhanceHorizontalEdges(result: MutableMap<Pair<Int, Int>, Int>) {
        for (x in minX-1..maxX+1) {
            val up = Pair(x, minY-1)
            result[up] = getEnhancedPixel(up)

            val down = Pair(x, maxY+1)
            result[down] = getEnhancedPixel(down)
        }
    }

    private fun enhanceVerticalEdges(result: MutableMap<Pair<Int, Int>, Int>) {
        for (y in minY..maxY) {
            val left = Pair(minX-1, y)
            result[left] = getEnhancedPixel(left)

            val right = Pair(maxX+1, y)
            result[right] = getEnhancedPixel(right)
        }
    }

    private fun nextDefaultPixel(): Int =
        (0 until 9).sumOf { defaultPixel * 2.pow(it) }.let { enhancer[it] }

    private fun getEnhancedPixel(p: Pair<Int, Int>): Int =
        neighborhood.withIndex().sumOf { (pixels[p.plus(it.value)] ?: defaultPixel) * 2.pow(it.index) }.let { enhancer[it] }

    companion object {
        fun fromFile(fileName: String): Image {
            val lines = readFileAsLines(fileName)
            val enhancer = lines[0].map { if (it == '#') 1 else 0 }
            val image = lines.drop(2)
            val map = (0..image.lastIndex)
                .flatMap { y -> (0..image[0].lastIndex).map { x -> Pair(x, y) }}
                .associateWith { if (image[it.second][it.first] == '#') 1 else 0 }
            return Image(map, enhancer)
        }
    }
}

val neighborhood = listOf(
    Pair(1, 1), Pair(0, 1), Pair(-1, 1),
    Pair(1, 0), Pair(0, 0), Pair(-1, 0),
    Pair(1, -1), Pair(0, -1), Pair(-1, -1),
)