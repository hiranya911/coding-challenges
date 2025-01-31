package utils

import java.io.File

fun readFileAsLines(fileName: String): List<String> =
    File(fileName).useLines { it.toList() }

fun readFileAsString(fileName: String): String = File(fileName).readText()