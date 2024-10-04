package org.shinytomato.convox.data

import org.shinytomato.convox.data.Language.Companion.WORD_PAGE_EXTENSION
import org.shinytomato.convox.data.LanguageConfig.Companion.WORD_DIR
import org.shinytomato.convox.data.ResourceManager.dictRooted
import org.shinytomato.convox.data.ResourceManager.stdRooted
import java.io.File

class LanguageConfig(
    val name: String,
    val classes: OrderingMap<Keyword>,
    val tags: OrderingMap<Keyword>,
) {

    fun getDir(): File = name.dictRooted()
    fun resolve(loc: String): File = name.dictRooted().resolve(loc)
    fun resolvePage(i: Int): File = resolve(WORD_DIR).resolve("${i.toString(16)}$WORD_PAGE_EXTENSION")

    fun toWriting(words: List<Word>): WritingConfig =
        WritingConfig(name, classes.toWriting(), tags.toWriting(), words)

    fun wordsFromDir(langName: String): MutableMap<String, MutableList<Word>> =
        wordsFromDir(langName.dictRooted())

    fun wordsFromDir(langDir: File): MutableMap<String, MutableList<Word>> =
        Word.fromDir(langDir.resolve(WORD_DIR), this)
            .groupBy { it.name }
            .mapValues { (_, v) -> v.toMutableList() }.toMutableMap()

    companion object {
        private const val CLASSES_FILE = "classes"
        private const val TAGS_FILE = "tags"
        const val WORD_DIR = "wrd"

        private fun attributesFromDir(langDir: File, x: String): OrderingMap<Keyword> {
            val file = langDir.resolve(x)
                .run { if (isFile) this else x.stdRooted() }

            return OrderingMap(file.bufferedReader()
                .use { br ->
                    br.lineSequence()
                        .map(String::trim)
                        .associate { s ->
                            val i = s.indexOf(':')
                            (s.substring(0..<i).toInt(16)) to Keyword(s.substring(i..<s.length))
                        }
                        .toMutableMap()
                })
        }

        private fun classesFromDir(langDir: File): OrderingMap<Keyword> =
            attributesFromDir(langDir, CLASSES_FILE)

        private fun tagsFromDir(langDir: File): OrderingMap<Keyword> =
            attributesFromDir(langDir, TAGS_FILE)

        fun fromDir(langName: String): LanguageConfig {
            val langDir = langName.dictRooted()
            return LanguageConfig(langName, classesFromDir(langDir), tagsFromDir(langDir))
        }
    }
}

data class WritingConfig(
    val name: String,
    val classes: WritingOrderingMap<Keyword>,
    val tags: WritingOrderingMap<Keyword>,
    val words: List<Word>,
) {

    fun getDir(): File = name.dictRooted()
    fun resolve(loc: String): File = name.dictRooted().resolve(loc)
    fun resolvePage(i: Int): File = resolve(WORD_DIR).resolve("${i.toString(16)}$WORD_PAGE_EXTENSION")

}