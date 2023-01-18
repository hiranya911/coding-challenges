package day7

import utils.readFileAsLines
import java.util.Stack

fun main() {
    val fs = buildFileSystem(readFileAsLines("inputs/d7_large.txt"))
    println("Small dirs: ${getTotalDirectorySize(fs)}")
    println("To delete: ${findSmallestDelete(fs)}")
}

fun getTotalDirectorySize(fs: FileSystem, limit: Int = 100000): Int {
    var smallDirs = 0
    fs.walkTree {
        if (it is Node.Dir && it.size() <= limit) {
            smallDirs += it.size()
        }
    }

    return smallDirs
}

fun findSmallestDelete(fs: FileSystem, total: Int = 70000000, requiredFree: Int = 30000000): Int {
    val currentFree = total - fs.usedSpace()
    val toFree = requiredFree - currentFree
    val candidates = mutableSetOf<Node>()
    fs.walkTree {
        if (it is Node.Dir && it.size() >= toFree) {
            candidates.add(it)
        }
    }

    return candidates.minOf { it.size() }
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
    abstract fun size(): Int

    data class Dir(override val name: String, val children: MutableList<Node>) : Node(name) {
        override fun size(): Int = children.sumOf { it.size() }
    }
    data class File(override val name: String, private val bytes : Int) : Node(name) {
        override fun size(): Int = bytes
    }
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

    fun walkTree(onVisit: (node: Node) -> Unit) {
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

    fun usedSpace(): Int = root.size()
}