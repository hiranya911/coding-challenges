package day17

const val SMALL = "target area: x=20..30, y=-10..-5"
const val LARGE = "target area: x=241..275, y=-75..-49"

fun main() {
    val target = TargetArea.fromString(LARGE)
    println("Max height = ${findMaxHeight(target)}")
    println("Valid initial velocities = ${findValidVelocities(target)}")
}

// Found this on Reddit. No clue why it works.
fun computeMaxHeight(target: TargetArea): Int = target.yrange.first.let { it * (it + 1) / 2 }

// Suppose target area is (X0..X1) and (Y0..Y1)
// Vx must be at least 1. Otherwise it won't trend towards the target. But it cannot exceed
// X1, since that will overshoot the target in just 1 step.
fun findMaxHeight(target: TargetArea): Int =
    (1..target.xrange.last)
        .flatMap { vx -> (1..target.xrange.last)
            .map { vy -> findMaxHeightAtVelocity(vx, vy, target) } }
        .filter { it.first }
        .maxOf { it.second }

// Vy cannot be lower than Y0. Otherwise it will overshoot the target in step 1.
// At the slowest speed with no drag, probe overshoots the target in X1 steps.
// Therefore Vy must be smaller than X1 (At Vy, probe requires 2*Vy steps to come
// back to the launch level; and the target area is below launch level).
fun findValidVelocities(target: TargetArea): Int =
    (1..target.xrange.last)
        .flatMap { vx -> (target.yrange.first..target.xrange.last)
            .map { vy -> findMaxHeightAtVelocity(vx, vy, target) } }
        .count { it.first }

fun findMaxHeightAtVelocity(vx: Int, vy: Int, target: TargetArea): Pair<Boolean, Int> {
    var maxHeight = -1
    val p = Probe(vx, vy)
    while (p.onCourseFor(target)) {
        p.step()
        if (p.inTargetArea(target)) {
            if (maxHeight < p.maxY) {
                maxHeight = p.maxY
            }

            return Pair(true, maxHeight)
        }
    }

    return Pair(false, -1)
}

class Probe(private var vx: Int, private var vy: Int) {
    private var x = 0
    private var y = 0
    var maxY = y

    fun step() {
        x += vx
        y += vy
        if (y > maxY) {
            maxY = y
        }

        if (vx > 0) {
            vx--
        } else if (vx < 0) {
            vx++
        }

        vy--
    }

    fun inTargetArea(target: TargetArea): Boolean = x in target.xrange && y in target.yrange

    fun onCourseFor(target: TargetArea): Boolean =
        y > target.yrange.first && x < target.xrange.last
}

class TargetArea(val xrange: IntRange, val yrange: IntRange) {
    companion object {
        fun fromString(s: String): TargetArea {
            val xrange = s.substringAfter("x=").substringBefore(", y").split("..").let{
                it[0].toInt()..it[1].toInt()
            }
            val yrange = s.substringAfter("y=").split("..").let{
                it[0].toInt()..it[1].toInt()
            }
            return TargetArea(xrange, yrange)
        }
    }
}