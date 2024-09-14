package org.shinytomato.convox.data

import org.shinytomato.convox.data.DataManager.stdDir
import java.io.File

fun String.splitAndTrim(delim: Char) = split(delim)
    .run { if (first().isEmpty()) subList(1, this.size) else this }

class Language(
    private val languageConfig: LanguageConfig,
    private val words: HashMap<String, WordValue>,
    //TODO: 동음이의어를 처리하는 방식
    // 1. 단어명으로 찾고 그 다음에 id로 찾는다 (동음이의어 관리자는 필요)
    // 2. 단어명으로 찾고 그 다음에 별개로 존재하는 동음이의어 관리자에서 그 동음이의어 Set의 id(위첨자)를 가지고 한다.
    //      -> 이 경우 동음이의어가 없었는데 나중에 생기는 경우엔?
    //      -> 그리고 순서를 정한다 해도 각각의 요소들을 어떻게 구별하지? -> 불가능
    // 3. 1번과 2번을 짬뽕해서 언어 자체 id와 동음이의어 용 id가 존재함.
    //      -> 동음이의어 관리자가 각 단어를 구별할 수 있고,
    //      -> 동음이의어 내에서 해당 단어를 찾을 때 id를 일일히 비교하지 않고 그냥 내부 id를 사용해 획득 가능
) {
    fun words() = words.toMap()

    val edited: HashSet<Word> = HashSet()
    val removed: HashSet<Word> = HashSet()

    override fun toString(): String {
        val sb = StringBuilder("Language(\n\tconfig=")
//        sb.appendLine(config.toString())
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
                    wrdDir.resolve((i++).toString()),
                    words,
                    keywordMap,
                )
            }
    }

    fun find(name: String, id: Int): Word? = words[name]?.findById(id)

    fun edit(name: String, id: Int): Word? =
        find(name, id)?.also {
            editInFile(it)
            edited.add(it)
        }
    fun edit(name: String, id: Int, editing: Word.() -> Unit): Unit =
        edit(name, id)?.editing() ?: Unit

    //TODO: 만약에
    // editing에서 org.shinytomato.convox.data.Word 내부 property에 접근할 수 없으면, DSL 만들어서 간접적으로 객체 제공
    // 예:
    //  org.shinytomato.convox.data.WordEditing {
    //      tags { tags ->
    //          tags.add("a") // 이런 식
    //      }
    //  }
    fun editInFile(word: Word) {
        //TODO:
        // size가 더 작아졌으면 그냥 앞당기고
        // size가 더 늘어났으면 늘어난 만큼 뒤로 하던가 or 나머지 앞당기고 이걸 맨 뒤에 놓음
    }

    fun updateWordValue(name: String, function: WordValue.() -> WordValue) {
        words[name] = words[name]?.function() ?: return
    }

    fun remove(name: String, id: Int) =
        updateWordValue(name) {
            val word = findById(id) ?: return@updateWordValue this
            removeInFile(word)
            removed.add(word)
            minusById(id)
        }

    fun removeInFile(word: Word) {
        //TODO:
        // 앞당기기
    }

    companion object {

        private const val WORD_DIR = "wrd"

        const val HEADER = '\u0001'
        const val DIVIDER = '\u0004'
        const val QUERY = '\u0005' // ?a
        const val ASSIGN = '\u0006' // =b
        const val DESCRIBE = '\u0007' // ()

        private const val WORDS_A_PAGE = 20

        fun readFromDir(langDir: File, languageConfig: LanguageConfig): Language =
            Language(
                languageConfig,
                HashMap(
                    Word.fromDir(langDir.resolve(WORD_DIR), languageConfig.classes)
                        .groupBy { it.name }
                        .mapValues { (_, v) -> WordValue.from(v) })
            )

        fun fromDir(langDir: File): Language = readFromDir(langDir, LanguageConfig.fromDir(langDir))

        // HEADER id ASSIGN name ASSIGN pronunciation ASSIGN tags DIVIDER QUERY attr ASSIGN value DIVIDER meanings
    }
}

data class LanguageConfig(
    val classes: MutableList<Keyword>,
    val tags: MutableList<Keyword>,
) {
    companion object {
        private const val CLASSES_FILE = "classes.dat"
        private const val TAGS_FILE = "tags.dat"

        private fun xFromFile(langDir: File, x: String): MutableList<Keyword> = langDir.resolve(x)
            .run { if (isFile) this else stdDir.resolve(x) }
            .bufferedReader()
            .use { br ->
                br.lines().map(String::trim)
            }
            .map { Keyword(it) }
            .toList()

        private fun classesFromDir(langDir: File): MutableList<Keyword> = xFromFile(langDir, CLASSES_FILE)
        private fun tagsFromDir(langDir: File): MutableList<Keyword> = xFromFile(langDir, TAGS_FILE)

        fun fromDir(langDir: File): LanguageConfig =
            LanguageConfig(classesFromDir(langDir), tagsFromDir(langDir))
    }
}

//TODO: display용 Word따로 - 동음이의어번호, Language 정보 포함
// -> Language는 Controller에서 보관, 동음이의어번호는 WordValue에서 내보낼 때 이름에 붙여서 보냄

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