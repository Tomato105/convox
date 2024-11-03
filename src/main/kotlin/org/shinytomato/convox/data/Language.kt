package org.shinytomato.convox.data

import org.shinytomato.convox.data.word.Word
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.chunked


fun String.splitAndTrim(delim: Char) =
    split(delim)
        .run { if (first().isEmpty()) subList(1, this.size) else this }

class Language(
    private val languageConfig: LanguageConfig,
    val words: HashMap<String, MutableList<Word>>,
) {

    private val edited: HashSet<Word> = HashSet()
    private val removed: HashSet<Word> = HashSet()

    override fun toString(): String {
        val sb = StringBuilder("Language(\n\tconfig=")
        sb.appendLine("\twords=[")
        for ((word, meanings) in words) {
            sb.appendLine("\t\t$word:$meanings")
            sb.appendLine()
        }
        return sb.toString()
    }

    fun save(languageConfig: LanguageConfig) {
        val words = words.values.flatten()
        saveToDir(languageConfig.toWriting(words), words)
    }

    fun edit(word: Word, editing: Word.() -> Unit) {
        word.editing()
        editInFile(word)
        edited.add(word)
    }

    fun remove(word: Word) {
        disableInFile(word)
        removed.add(word)
    }

    private fun editInFile(word: Word) {
        //TODO:
        // size가 더 작아졌으면 그냥 앞당기고
        // size가 더 늘어났으면 늘어난 만큼 뒤로 하던가 or 나머지 앞당기고 이걸 맨 뒤에 놓음
    }

    private fun disableInFile(word: Word) {
        RandomAccessFile(languageConfig.resolvePage(word.page), "rw").use { f ->
            f.seek(word.loc.toLong())
            f.write(4)
        }
    }

    companion object {

        const val WORD_PAGE_EXTENSION = ".convox"
        private const val WORDS_A_PAGE = 20

        private fun readFromDir(languageDir: File, languageConfig: LanguageConfig): Language =
            Language(
                languageConfig,
                languageConfig.wordsFromDir(languageDir)
            )

        fun fromDir(languageDir: File): Language =
            readFromDir(languageDir, LanguageConfig.fromDir(languageDir))

        private fun saveToString(words: List<Word>, writingConfig: WritingConfig) =
            words.joinToString("") { it.write(writingConfig) }

        private fun saveToFile(file: File, words: List<Word>, writingConfig: WritingConfig): Unit =
            file.bufferedWriter().use { bw ->
                bw.write(saveToString(words, writingConfig))
            }

        fun saveToDir(writingConfig: WritingConfig, words: List<Word>) {
            var i = 0

            words
                .chunked(WORDS_A_PAGE)
                .forEach { words ->
                    saveToFile(
                        writingConfig.resolvePage(i),
                        words,
                        writingConfig,
                    )
                }
        }
    }
}
