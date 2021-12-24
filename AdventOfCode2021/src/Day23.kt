package day23

import java.util.*

fun main() {
    val realPlacement1 = mapOf(
        100 to "B", 101 to "C",
        200 to "A", 201 to "A",
        300 to "D", 301 to "B",
        400 to "C", 401 to "D",
    )
    val realPlacement2 = mapOf(
        100 to "B", 101 to "D", 102 to "D", 103 to "C",
        200 to "A", 201 to "B", 202 to "C", 203 to "A",
        300 to "D", 301 to "A", 302 to "B", 303 to "B",
        400 to "C", 401 to "C", 402 to "A", 403 to "D",
    )

    val burrow1 = Burrow(realPlacement1, BasicLocationSetup())
    println("\nLeast energy = ${relocate(burrow1)}")
    println()

    val burrow2 = Burrow(realPlacement2, ExtendedLocationSetup())
    println("\nLeast energy (extended) = ${relocate(burrow2)}")
}

fun relocate(init: Burrow): Long {
    val frontier = PriorityQueue(compareByCost).apply { add(init) }
    val explored = mutableMapOf(init.placement to 0L)
    var iter = 0
    while (frontier.isNotEmpty()) {
        val curr = frontier.remove()
        iter++
        if (iter % 10000 == 0) {
            println("$iter: ${curr.placement} (${curr.energy})")
        }

        if (curr.isGoal()) {
            return curr.energy
        } else {
            curr.successors().forEach {
                if (it.placement !in explored || explored[it.placement]!! > it.energy) {
                    explored[it.placement] = it.energy
                    frontier.add(it)
                }
            }
        }
    }

    return -1
}

interface LocationSetup {
    val goal: Map<Int, String>
    fun neighbors(curr: Int): List<Int>
    fun targets(name: String): List<Int>
}

class Burrow(
    val placement: Map<Int, String>,
    private val setup: LocationSetup,
    val energy: Long = 0,
) {

    fun isGoal(): Boolean =
        placement.mapValues { it.value.substring(0, 1) } == setup.goal

    fun successors(): List<Burrow> {
        val result = mutableListOf<Burrow>()
        placement.forEach { (curr, name) ->
            successorsFor(name, curr).filterNot { dest -> dest in placement }.forEach { dest ->
                val (hasPath, moves) = hasClearPath(curr, dest)
                if (hasPath) {
                    val newPlacement = placement.toMutableMap()
                    newPlacement.remove(curr)
                    newPlacement[dest] = name
                    result.add(Burrow(newPlacement, setup, energy + energy(name, moves)))
                }
            }
        }

        return result
    }

    private fun successorsFor(name: String, curr: Int): List<Int> {
        if (curr < 100) {
            // in hallway
            return getRoomTargets(name)
        }

        // In one of the rooms
        val rooms = setup.targets(name)
        if (curr !in rooms) {
            // In the wrong rooms
            return listOf(0, 1, 3, 5, 7, 9, 10).plus(getRoomTargets(name))
        }

        // We are blocking another type of creature. Have to move out and unblock.
        if (isBlockingOther(name, curr, rooms)) {
            return listOf(0, 1, 3, 5, 7, 9, 10)
        }

        // We are not blocking anyone. Just move as inwards as possible in the room.
        return (curr-1 downTo rooms[0]).toList()
    }

    private fun isBlockingOther(name: String, curr: Int, rooms: List<Int>): Boolean =
        (curr-1 downTo rooms[0]).any { it in placement && placement[it]!! != name }

    private fun getRoomTargets(name: String): List<Int> {
        val targets = setup.targets(name)
        val otherOccupants = targets.any { it in placement && placement[it]!! != name }
        if (otherOccupants) {
            return listOf()
        }

        return targets
    }

    private fun hasClearPath(from: Int, to: Int): Pair<Boolean, Int> {
        val frontier = Stack<Pair<Int, Int>>().apply { push(Pair(from, 0)) }
        val explored = mutableSetOf(from)
        while (frontier.isNotEmpty()) {
            val curr = frontier.pop()
            if (curr.first == to) {
                return Pair(true, curr.second)
            }

            setup.neighbors(curr.first).filterNot { it in placement }.forEach {
                if (it !in explored) {
                    explored.add(it)
                    frontier.push(Pair(it, curr.second + 1))
                }
            }
        }

        return Pair(false, -1)
    }

    private fun energy(name: String, moves: Int): Long {
        return when (name) {
            "A" -> moves * 1L
            "B" -> moves * 10L
            "C" -> moves * 100L
            "D" -> moves * 1000L
            else -> throw Exception("invalid name $name")
        }
    }
}

val compareByCost: Comparator<Burrow> = compareBy { it.energy }

class BasicLocationSetup: LocationSetup {
    override fun neighbors(curr: Int): List<Int> {
        return when (curr) {
            0 -> listOf(1)
            1 -> listOf(0, 2)
            2 -> listOf(1, 3, 101)
            3 -> listOf(2, 4)
            4 -> listOf(3, 5, 201)
            5 -> listOf(4, 6)
            6 -> listOf(5, 7, 301)
            7 -> listOf(6, 8)
            8 -> listOf(7, 9, 401)
            9 -> listOf(8, 10)
            10 -> listOf(9)
            101 -> listOf(2, 100)
            100 -> listOf(101)
            201 -> listOf(4, 200)
            200 -> listOf(201)
            301 -> listOf(6, 300)
            300 -> listOf(301)
            401 -> listOf(8, 400)
            400 -> listOf(401)
            else -> throw Exception("invalid location $curr")
        }
    }

    override fun targets(name: String): List<Int> {
        return when (name) {
            "A" -> listOf(100, 101)
            "B" -> listOf(200, 201)
            "C" -> listOf(300, 301)
            "D" -> listOf(400, 401)
            else -> throw Exception("invalid name $name")
        }
    }

    override val goal = mapOf(
        100 to "A", 101 to "A",
        200 to "B", 201 to "B",
        300 to "C", 301 to "C",
        400 to "D", 401 to "D",
    )
}

class ExtendedLocationSetup: LocationSetup {
    override fun neighbors(curr: Int): List<Int> {
        return when (curr) {
            0 -> listOf(1)
            1 -> listOf(0, 2)
            2 -> listOf(1, 3, 103)
            3 -> listOf(2, 4)
            4 -> listOf(3, 5, 203)
            5 -> listOf(4, 6)
            6 -> listOf(5, 7, 303)
            7 -> listOf(6, 8)
            8 -> listOf(7, 9, 403)
            9 -> listOf(8, 10)
            10 -> listOf(9)
            103 -> listOf(2, 102)
            100 -> listOf(101)
            101 -> listOf(100, 102)
            102 -> listOf(101, 103)
            203 -> listOf(4, 202)
            200 -> listOf(201)
            201 -> listOf(200, 202)
            202 -> listOf(201, 203)
            303 -> listOf(6, 302)
            300 -> listOf(301)
            301 -> listOf(300, 302)
            302 -> listOf(301, 303)
            403 -> listOf(8, 402)
            400 -> listOf(401)
            401 -> listOf(400, 402)
            402 -> listOf(401, 403)
            else -> throw Exception("invalid location $curr")
        }
    }

    override fun targets(name: String): List<Int> {
        return when (name) {
            "A" -> listOf(100, 101, 102, 103)
            "B" -> listOf(200, 201, 202, 203)
            "C" -> listOf(300, 301, 302, 303)
            "D" -> listOf(400, 401, 402, 403)
            else -> throw Exception("invalid name $name")
        }
    }

    override val goal = mapOf(
        100 to "A", 101 to "A", 102 to "A", 103 to "A",
        200 to "B", 201 to "B", 202 to "B", 203 to "B",
        300 to "C", 301 to "C", 302 to "C", 303 to "C",
        400 to "D", 401 to "D", 402 to "D", 403 to "D",
    )
}