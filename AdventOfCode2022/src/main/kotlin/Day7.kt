package day7

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val fs = buildFileSystem(readFileAsLines("inputs/d7_large.txt"))
    val dirs = getDirectorySizes(fs)
    println("Small dirs: ${dirs.filter { it <= 100000 }.sum()}")

    val currentFree = 70000000 - dirs.max()
    val toFree = 30000000 - currentFree
    println("To delete: ${dirs.filter { it >= toFree }.min()}")
}

fun getDirectorySizes(fs: FileSystem): List<Int> {
    val dirs = fs.dirs()
    val cache = mutableMapOf<Node.Dir, Int>()

    fun getOrComputeSize(d: Node.Dir): Int {
        return cache[d] ?: run {
            val computed = d.children.sumOf {
                when (it) {
                    is Node.File -> it.size
                    is Node.Dir -> getOrComputeSize(it)
                }
            }
            cache[d] = computed
            return computed
        }
    }

    dirs.forEach { getOrComputeSize(it) }
    return cache.values.toList()
}

fun buildFileSystem(lines: List<String>): FileSystem {
    val fs = FileSystem()
    for (line in lines) {
        if (line.startsWith("$ cd")) {
            val arg = line.split(" ").last()
            fs.changeDirectory(arg)
        } else if (line == "$ ls") {
            continue
        } else if (line.startsWith("dir")) {
            val name = line.split(" ").last()
            fs.mkdir(name)
        } else {
            val (size, name) = line.split(" ")
            fs.addFile(name, size.toInt())
        }
    }

    return fs
}

sealed class Node(open val name: String) {
    data class Dir(override val name: String, val children: MutableList<Node>) : Node(name)
    data class File(override val name: String, val size : Int) : Node(name)
}

class FileSystem {
    private val root = Node.Dir("/", children = mutableListOf())
    private val path = Stack<Node.Dir>()
    private var current: Node.Dir = root

    fun changeDirectory(arg: String) {
        current = when (arg) {
            "/" -> root
            ".." -> path.pop()
            else -> {
                val child = current.children.filterIsInstance<Node.Dir>().find { it.name == arg } ?: throw IllegalArgumentException("No such child: $arg")
                path.push(current)
                child
            }
        }
    }

    fun mkdir(name: String) {
        current.children.find { it.name == name }?.let { throw IllegalArgumentException("File already exits: $name") }
        current.children.add(Node.Dir(name, children = mutableListOf()))
    }

    fun addFile(name: String, size: Int) {
        current.children.find { it.name == name }?.let { throw IllegalArgumentException("File already exits: $name") }
        current.children.add(Node.File(name, size))
    }

    fun dirs(): List<Node.Dir> {
        val result = mutableListOf<Node.Dir>()
        walkTree {
            if (it is Node.Dir) {
                result.add(it)
            }
        }

        return result
    }

    private fun walkTree(onVisit: (node: Node) -> Unit) {
        val frontier = Stack<Node>()
        frontier.push(root)
        while (frontier.isNotEmpty()) {
            val current = frontier.pop()
            onVisit(current)
            if (current is Node.Dir) {
                current.children.forEach { frontier.push(it) }
            }
        }
    }
}