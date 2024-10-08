package org.shinytomato.convox.data

import java.io.File
import java.net.URL

object ResourceManager {
    const val DATA_ROOT = "data/"
    const val DICT_ROOT = "${DATA_ROOT}dict/"
    const val STD_ROOT = "${DATA_ROOT}std/"
    const val INDEX_DAT = "index.dat"

    val dataDir = File("data")
    val dictDir = dataDir.resolve("dict")
    val stdDir = dataDir.resolve("std")

    fun languageDirList(): List<File> =
        INDEX_DAT.dataRooted().bufferedReader().use { br ->
            br.lineSequence()
                .map(String::trim)
                .toSet()
        }
            .plus(dictDir.list())
            .map { it.dictRooted() }
            .filter(File::isDirectory)

    fun String.dataRooted(): File = dataDir.resolve(this)
    fun String.dataRootedString(): String = "$DATA_ROOT/$this"
    fun String.dictRooted(): File = dictDir.resolve(this)
    fun String.dictRootedString(): String = "$DICT_ROOT/$this"
    fun String.stdRooted(): File = stdDir.resolve(this)
    fun String.stdRootedString(): String = "$STD_ROOT/$this"

    fun Any.resolveResource(string: String): URL? = this.javaClass.getResource(string)
    fun Any.resolveResourcePath(string: String): String? = this.resolveResource(string)?.toString()
}
