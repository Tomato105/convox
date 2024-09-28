package org.shinytomato.convox.data

import java.io.File
import java.net.URL

object ResourceManager {
    const val DATA_ROOT = "data/"
    const val DICT_ROOT = "${DATA_ROOT}dict/"
    const val STD_ROOT = "${DATA_ROOT}std/"

    val dataDir = File("data")
    val dictDir = dataDir.resolve("dict")
    val stdDir = dataDir.resolve("std")


    fun loadLanguageSet(): Set<String> =
        File("data/index.dat").bufferedReader().use { br ->
            br.lines()
                ?.map { it.trim() }
                ?.filter { it.dictRooted().isDirectory }
                ?.toList()
        }
            ?.toMutableSet()
            ?.plus(dictDir.listFiles().filter(File::isDirectory).map(File::getName)) ?: mutableSetOf()


    fun String.dictRooted(): File = dictDir.resolve(this)
    fun String.dictRootedString(): String = "$DICT_ROOT/$this"
    fun String.stdRooted(): File = stdDir.resolve(this)
    fun String.stdRootedString(): String = "$STD_ROOT/$this"
    fun Any.resolveResource(string: String): URL? = this.javaClass.getResource(string)
    fun Any.resolveResourcePath(string: String): String? = this.resolveResource(string)?.toString()
}
