package day1

import java.io.File

fun readFileAsLines(fileName: String): List<String> =
    File(fileName).useLines { it.toList() }