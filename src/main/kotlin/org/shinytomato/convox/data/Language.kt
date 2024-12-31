package org.shinytomato.convox.data

import org.shinytomato.convox.data.word.Word
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.chunked


fun String.splitAndTrim(delim: Char) =
    split(delim)
        .run { if (first().isEmpty()) subList(1, this.size) else this }

// 어차피 파일 크기 작으니까 굳이 복잡하게 중간 수정 하지 말고 파일 전체 수정
// -> 이걸 위해서 단어들을 Page로 묶어서 저장?
// -> edited, removed 불필요해질듯?
// 일단 1.작성중이던내용(갑자기 꺼짐 대비) & 2.변경내용(나중에 한번에적용)-다른파일에써놓기
class Language(
    private val languageConfig: LanguageConfig,
    val words: HashMap<String, MutableList<Word>>,
) {

    private val edited: HashSet<Word> = HashSet()
    private val removed: HashSet<Word> = HashSet()

    val name
        get() = languageConfig.name

    override fun toString(): String = """
        Language(
            config=$languageConfig,
            words=${words.map { (k, v) -> "$k[${v.joinToString(",") { it.id.toString() }}]" }}
        )""".trimIndent()

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
