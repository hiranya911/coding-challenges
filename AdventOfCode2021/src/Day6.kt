package day6

const val SMALL = "3,4,3,1,2"
const val LARGE = "1,3,3,4,5,1,1,1,1,1,1,2,1,4,1,1,1,5,2,2,4,3,1,1,2,5,4,2,2,3,1,2,3,2,1,1,4,4,2,4,4,1,2,4,3,3,3,1,1,3,4,5,2,5,1,2,5,1,1,1,3,2,3,3,1,4,1,1,4,1,4,1,1,1,1,5,4,2,1,2,2,5,5,1,1,1,1,2,1,1,1,1,3,2,3,1,4,3,1,1,3,1,1,1,1,3,3,4,5,1,1,5,4,4,4,4,2,5,1,1,2,5,1,3,4,4,1,4,1,5,5,2,4,5,1,1,3,1,3,1,4,1,3,1,2,2,1,5,1,5,1,3,1,3,1,4,1,4,5,1,4,5,1,1,5,2,2,4,5,1,3,2,4,2,1,1,1,2,1,2,1,3,4,4,2,2,4,2,1,4,1,3,1,3,5,3,1,1,2,2,1,5,2,1,1,1,1,1,5,4,3,5,3,3,1,5,5,4,4,2,1,1,1,2,5,3,3,2,1,1,1,5,5,3,1,4,4,2,4,2,1,1,1,5,1,2,4,1,3,4,4,2,1,4,2,1,3,4,3,3,2,3,1,5,3,1,1,5,1,2,2,4,4,1,2,3,1,2,1,1,2,1,1,1,2,3,5,5,1,2,3,1,3,5,4,2,1,3,3,4"

fun main() {
    val school = FishSchool.fromString(LARGE)
    for (i in 0 until 256) {
        school.tick()
        if (i == 79) {
            println("After 80 days = ${school.total()}")
        }
    }

    println("After 256 days = ${school.total()}")
}

class FishSchool(private var map: Map<Int, Long>) {

    fun tick() {
        val temp = mutableMapOf<Int, Long>()
        (8 downTo 1).forEach {
            temp[it-1] = map.getOrDefault(it, 0)
        }

        val zeros = map.getOrDefault(0, 0)
        temp[6] = (temp[6] ?: 0) + zeros
        temp[8] = zeros
        map = temp
    }

    fun total(): Long = map.values.sum()

    companion object {
        fun fromString(str: String): FishSchool =
            str.split(",").map { it.toInt() }.groupingBy { it }.eachCount()
                .mapValues { it.value.toLong() }.let { FishSchool(it) }
    }
}