package org.shinytomato.convox.data

import org.shinytomato.convox.data.Language.Companion.WORD_PAGE_EXTENSION
import org.shinytomato.convox.data.LanguageConfig.Companion.WORD_DIR_NAME
import org.shinytomato.convox.data.ResourceManager.dictRooted
import org.shinytomato.convox.data.ResourceManager.stdRooted
import org.shinytomato.convox.toHashMap
import org.shinytomato.convox.data.word.Keyword
import org.shinytomato.convox.data.word.OrderingMap
import org.shinytomato.convox.data.word.Word
import org.shinytomato.convox.data.word.WritingOrderingMap
import java.io.File

class LanguageConfig(
    val name: String,
    val classes: OrderingMap<Keyword>,
    val tags: OrderingMap<Keyword>,
) {

    fun resolve(loc: String): File = name.dictRooted().resolve(loc)
    fun resolvePage(i: Int): File = resolve(WORD_DIR_NAME).resolve("${i.toString(16)}$WORD_PAGE_EXTENSION")

    fun toWriting(words: List<Word>): WritingConfig =
        WritingConfig(name, classes.toWriting(), tags.toWriting(), words)

    fun wordsFromDir(langDir: File): HashMap<String, MutableList<Word>> =
        Word.Companion.fromDir(langDir.resolve(WORD_DIR_NAME), this)
            .groupBy { it.name }
            .mapValues { (_, v) -> v.toMutableList() }
            .toHashMap()

    override fun toString() = "LanguageConfig(name=$name, classes=$classes, tags=$tags"

    companion object {
        private const val CLASSES_FILE_NAME = "classes"
        private const val TAGS_FILE_NAME = "tags"
        const val WORD_DIR_NAME = "wrd"

        private fun linesToAttributes(langDir: File, fileName: String): OrderingMap<Keyword> {
            val file = langDir.resolve(fileName)
                .run { if (isFile) this else fileName.stdRooted() }

            return OrderingMap(file.bufferedReader()
                .use { br ->
                    br.lineSequence()
                        .mapNotNull { s ->
                            val s = s.trim()
                            val i = s.indexOf(':')
                            ((s.substring(0..<i).toIntOrNull(16)) ?: return@mapNotNull null) to Keyword(s.substring((i+1)..<s.length))
                        }
                        .toMap()
                        .toHashMap()
                })
        }

        private fun classesFromDir(langDir: File): OrderingMap<Keyword> =
            linesToAttributes(langDir, CLASSES_FILE_NAME)

        private fun tagsFromDir(langDir: File): OrderingMap<Keyword> =
            linesToAttributes(langDir, TAGS_FILE_NAME)

        fun fromDir(langDir: File): LanguageConfig {
            return LanguageConfig(langDir.name, classesFromDir(langDir), tagsFromDir(langDir))
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
    fun resolvePage(i: Int): File = resolve(WORD_DIR_NAME).resolve("${i.toString(16)}$WORD_PAGE_EXTENSION")

}