package org.shinytomato.convox.data

import org.shinytomato.convox.data.ResourceManager.stdRooted
import java.io.File

fun String.splitAndTrim(delim: Char) = split(delim)
    .run { if (first().isEmpty()) subList(1, this.size) else this }

class Language(
    private val languageConfig: LanguageConfig,
    private val words: HashMap<String, WordValue>,
) {
    fun words() = words.toMap()

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

    private fun saveToString(words: List<Word>, wordClassMapping: KeywordMap) =
        words.joinToString("") { it.write(wordClassMapping) }

    private fun saveToFile(file: File, words: List<Word>, wordClassMapping: KeywordMap): Unit =
        file.bufferedWriter().use { bw ->
            bw.write(saveToString(words, wordClassMapping))
        }

    fun saveToDir(wrdDir: File) {
        val keywordMap = KeywordMap(languageConfig.classes)
        var i = 0

        words.values
            .flatMap(WordValue::toList)
            .chunked(WORDS_A_PAGE)
            .forEach { words ->
                saveToFile(
                    wrdDir.resolve("${i++}$PAGE_EXTENDER"),
                    words,
                    keywordMap,
                )
            }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun find(name: String, id: Int): Word? = words[name]?.findById(id)

    private fun getEdited(name: String, id: Int): Word? =
        find(name, id)?.also {
            editInFile(it)
            edited.add(it)
        }

    fun edit(word: Word, editing: Word.() -> Unit): Unit =
        edit(word.name, word.id, editing)

    fun edit(name: String, id: Int, editing: Word.() -> Unit): Unit =
        getEdited(name, id)?.editing() ?: Unit

    private fun editInFile(word: Word) {
        //TODO:
        // size가 더 작아졌으면 그냥 앞당기고
        // size가 더 늘어났으면 늘어난 만큼 뒤로 하던가 or 나머지 앞당기고 이걸 맨 뒤에 놓음
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun updateWordValue(name: String, function: WordValue.() -> WordValue) {
        words[name] = words[name]?.function() ?: return
    }

    fun remove(name: String, id: Int) = updateWordValue(name) {
        val word = findById(id) ?: return@updateWordValue this
        removeInFile(word)
        removed.add(word)
        minusById(id)
    }


    private fun removeInFile(word: Word) {
        //TODO:
        // 앞당기기
    }

    companion object {

        private const val WORD_DIR = "wrd"
        private const val PAGE_EXTENDER = ".dat"

        private const val WORDS_A_PAGE = 20

        private fun readFromDir(langDir: File, languageConfig: LanguageConfig): Language =
            Language(
                languageConfig,
                HashMap(
                    Word.fromDir(langDir.resolve(WORD_DIR), languageConfig.classes)
                        .groupBy { it.name }
                        .mapValues { (_, v) -> WordValue.from(v) })
            )

        fun fromDir(langDir: File): Language =
            readFromDir(langDir, LanguageConfig.fromDir(langDir))
    }
}

data class LanguageConfig(
    val classes: MutableList<Keyword>,
    val tags: MutableList<Keyword>,
) {
    companion object {
        private const val CLASSES_FILE = "classes.dat"
        private const val TAGS_FILE = "tags.dat"

        private fun attributesFromDir(langDir: File, x: String): MutableList<Keyword> {
            val file = langDir.resolve(x)
                .run { if (isFile) this else x.stdRooted() }

            return if (file.isFile)
                file
                    .bufferedReader()
                    .use { br ->
                        br.lineSequence()
                            .map(String::trim)
                            .map(::Keyword)
                            .toMutableList()
                    }
            else mutableListOf()
        }

        private fun classesFromDir(langDir: File): MutableList<Keyword> = attributesFromDir(langDir, CLASSES_FILE)
        private fun tagsFromDir(langDir: File): MutableList<Keyword> = attributesFromDir(langDir, TAGS_FILE)

        fun fromDir(langDir: File): LanguageConfig =
            LanguageConfig(classesFromDir(langDir), tagsFromDir(langDir))
    }
}

sealed class WordValue {

    abstract override fun toString(): String
    abstract fun plus(word: Word): WordValue
    abstract fun minus(word: Word): WordValue
    abstract fun minusById(id: Int): WordValue
    abstract fun minusByHomonymId(homonymId: Int): WordValue
    abstract fun write(word: Word): String
    abstract fun toList(): List<Word>
    abstract fun findById(id: Int): Word?
    abstract fun findByHomonymId(homonymId: Int): Word?

    object Empty : WordValue() {
        override fun toString(): String = "Empty"
        override fun plus(word: Word): Single = Single(word)
        override fun minus(word: Word): WordValue = this
        override fun minusById(id: Int): WordValue = this
        override fun minusByHomonymId(homonymId: Int): WordValue = this
        override fun write(word: Word): String = throw IllegalStateException("cannot write WordValue.Empty")
        override fun toList(): List<Word> = listOf()
        override fun findById(id: Int): Word? = null
        override fun findByHomonymId(homonymId: Int): Word? = null
    }

    class Single(val word: Word) : WordValue() {
        override fun toString(): String = "Single($word)"
        override fun plus(word: Word): Homonyms = Homonyms(mutableListOf(this.word, word))
        override fun minus(word: Word): WordValue =
            if (word === this.word) Empty
            else this

        override fun minusById(id: Int): WordValue =
            if (isIdIdentical(id)) Empty
            else this

        override fun minusByHomonymId(homonymId: Int): Empty = Empty
        override fun write(word: Word): String = "$word"
        override fun toList(): List<Word> = listOf(word)
        override fun findById(id: Int): Word? =
            if (isIdIdentical(id)) word else null

        override fun findByHomonymId(homonymId: Int): Word = word
        fun isIdIdentical(id: Int) = word.id == id
    }

    class Homonyms(private val words: MutableList<Word>) : WordValue() {
        override fun toString(): String = "Homonyms($words)"
        override fun plus(word: Word): Homonyms = this.apply { words.add(word) }
        override fun minus(word: Word): WordValue = minusByHomonymId(words.indexOf(word))
        override fun minusById(id: Int): WordValue = minusByHomonymId(words.find { it.id == id }?.id ?: -1)
        override fun minusByHomonymId(homonymId: Int): WordValue =
            this.apply { words.removeAt(homonymId) }.toSingleIfSingle()

        override fun write(word: Word): String = words.joinToString("")
        override fun toList(): List<Word> = words.toList()
        override fun findById(id: Int): Word? = words.find { it.id == id }
        override fun findByHomonymId(homonymId: Int): Word? = words.getOrNull(homonymId)

        private fun toSingleIfSingle(): WordValue =
            if (words.size == 1) Single(words.first()) else this
    }

    companion object {
        fun from(words: Collection<Word>) =
            if (words.size == 1) Single(words.first())
            else Homonyms(words.toMutableList())
    }
}