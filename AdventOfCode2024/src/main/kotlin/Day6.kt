package day6

fun main() {
    val patrol = PatrolState.fromFile("inputs/day6.txt")
    findLoopSources(patrol)
}

fun trackPatrolRoute(patrol: PatrolState): List<Pair<Int, Int>>? {
    var current: GuardState? = patrol.guard
    val visited = mutableSetOf<GuardState>()
    while (current != null) {
        if (!visited.add(current)) {
            return null
        }
        current = current.next(patrol.patrolMap)
    }

    return visited.map { it.currentLocation }
}

fun findLoopSources(patrol: PatrolState) {
    val route = trackPatrolRoute(patrol) ?: throw IllegalStateException("Loop found in initial config")
    val locationsOnPath = route.toSet()
    println("Visited locations: ${locationsOnPath.size}")

    val startingPos = patrol.guard.currentLocation
    val sources = locationsOnPath.mapNotNull {loc ->
        if (loc == startingPos) {
            return@mapNotNull null
        }

        val newState = patrol.copy(
            patrolMap = patrol.patrolMap.withObstructionAt(loc)
        )
        loc.takeIf { trackPatrolRoute(newState) == null }
    }
    println("Loop sources: ${sources.size}")
}

enum class LocationStatus {
    CLEAR,
    OBSTRUCTED,
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

data class PatrolMap(
    val locations: Map<Pair<Int, Int>, LocationStatus>,
) {
    fun get(pos: Pair<Int, Int>): LocationStatus {
        return locations.getValue(pos)
    }

    fun getOrNull(pos: Pair<Int, Int>): LocationStatus? {
        return locations[pos]
    }

    fun withObstructionAt(pos: Pair<Int, Int>): PatrolMap {
        check(pos in locations)
        val newLocations = locations.toMutableMap()
        newLocations[pos] = LocationStatus.OBSTRUCTED
        return PatrolMap(newLocations)
    }
}


data class GuardState(
    val currentLocation: Pair<Int, Int>,
    val direction: Direction,
) {
    companion object {
        fun fromChar(ch: Char, pos: Pair<Int, Int>): GuardState {
            return when (ch) {
                '^' -> GuardState(pos, Direction.UP)
                'v' -> GuardState(pos, Direction.DOWN)
                '<' -> GuardState(pos, Direction.LEFT)
                '>' -> GuardState(pos, Direction.RIGHT)
                else -> throw IllegalArgumentException("Invalid character for guard: $ch")
            }

        }
    }

    fun next(map: PatrolMap): GuardState? {
        val loc = when (direction) {
            Direction.UP -> Pair(currentLocation.first, currentLocation.second - 1)
            Direction.DOWN -> Pair(currentLocation.first, currentLocation.second + 1)
            Direction.LEFT -> Pair(currentLocation.first - 1, currentLocation.second)
            Direction.RIGHT -> Pair(currentLocation.first + 1, currentLocation.second)
        }
        val status = map.getOrNull(loc) ?: return null
        return if (status == LocationStatus.OBSTRUCTED) {
            rotateRight()
        } else {
            copy(currentLocation = loc)
        }
    }

    private fun rotateRight(): GuardState {
        return copy(direction = when (direction) {
            Direction.UP -> Direction.RIGHT
            Direction.DOWN -> Direction.LEFT
            Direction.LEFT -> Direction.UP
            Direction.RIGHT -> Direction.DOWN
        })
    }
}

data class PatrolState(
    val patrolMap: PatrolMap,
    val guard: GuardState,
) {
    companion object {
        fun fromFile(fileName: String): PatrolState {
            val lines = java.io.File(fileName).readLines()
            val locations = mutableMapOf<Pair<Int, Int>, LocationStatus>()
            var guard: GuardState? = null
            for (y in lines.indices) {
                for (x in lines[y].indices) {
                    val pos = Pair(x, y)
                    when (lines[y][x]) {
                        '.' -> locations[pos] = LocationStatus.CLEAR
                        '#' -> locations[pos] = LocationStatus.OBSTRUCTED
                        '^', 'v', '<', '>' -> {
                            if (guard != null) {
                                throw IllegalArgumentException("Multiple guards found in map")
                            }
                            guard = GuardState.fromChar(lines[y][x], pos)
                            locations[pos] = LocationStatus.CLEAR
                        }
                        else -> throw IllegalArgumentException("Invalid character in map: ${lines[y][x]}")
                    }
                }
            }

            return PatrolState(
                patrolMap = PatrolMap(locations),
                guard = guard ?: throw IllegalArgumentException("No guard found in map"),
            )
        }
    }

    override fun toString(): String {
        val maxX = patrolMap.locations.keys.maxOf { it.first }
        val maxY = patrolMap.locations.keys.maxOf { it.second }
        val sb = StringBuilder()
        for (y in 0..maxY) {
            for (x in 0..maxX) {
                val pos = Pair(x, y)
                if (pos == guard.currentLocation) {
                    sb.append(when (guard.direction) {
                        Direction.UP -> '^'
                        Direction.DOWN -> 'v'
                        Direction.LEFT -> '<'
                        Direction.RIGHT -> '>'
                    })
                } else {
                    val status = patrolMap.get(pos)
                    sb.append(when (status) {
                        LocationStatus.CLEAR -> '.'
                        LocationStatus.OBSTRUCTED -> '#'
                    })
                }
            }
            sb.append('\n')
        }
        return sb.toString()
    }
}