package org.shinytomato.convox.data

import org.shinytomato.convox.data.Language.Companion.ASSIGN
import org.shinytomato.convox.data.Language.Companion.DESCRIBE
import org.shinytomato.convox.data.Language.Companion.DIVIDER
import org.shinytomato.convox.data.Language.Companion.HEADER
import org.shinytomato.convox.data.Language.Companion.QUERY
import java.io.File


class Word(
    val id: Int,
    name: String,
    private val tags: MutableSet<Keyword>,
    private val attrs: MutableMap<RelativeAttribute, MutableList<String>>,
    private val meanings: MutableMap<Keyword, MutableList<Meaning>>,
    val page: Int,
    val loc: Int,
    val size: Int,
) {
    var name = name
        private set

    fun tags(): Set<Keyword> = tags.toSet()
    fun attrs(): Map<RelativeAttribute, MutableList<String>> = attrs.toMap()
    fun meanings(): Map<Keyword, MutableList<Meaning>> = meanings.toMap()

    fun registerTag(language: Language, tag: Keyword): Unit =
        language.edit(name, id) { tags.add(tag) }
    fun unregisterTag(language: Language, tag: Keyword): Unit =
        language.edit(name, id) { tags.remove(tag) }

    fun registerMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(name, id) { meanings[wordClass]?.add(meaning) }
    fun unregisterMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(name, id) { meanings[wordClass]?.remove(meaning) }

    fun editTags(language: Language, editing: MutableSet<Keyword>.() -> Unit): Unit =
        language.edit(name, id) { this.tags.editing() }
    fun editAttrs(language: Language, editing: MutableMap<RelativeAttribute, MutableList<String>>.() -> Unit): Unit =
        language.edit(name, id) { this.attrs.editing() }
    fun editMeanings(language: Language, editing: MutableMap<Keyword, MutableList<Meaning>>.() -> Unit): Unit =
        language.edit(name, id) { this.meanings.editing() }

    fun write(wordClassMapping: KeywordMap): String {
        return "$HEADER$id" +
                "$ASSIGN$name" +
                "$ASSIGN${tags.valuesJoined()}" +
                "$DIVIDER" +
                writeQueries(attrs, RelativeAttribute::symbol) { it } +
                "$DIVIDER" +
                writeQueries(meanings, { it: Keyword -> it.code(wordClassMapping) ?: -1 }, Meaning::write)
    }

    @JvmName("CollectionKeywordValueJoined")
    private fun Collection<Keyword>.valuesJoined(): String = map { it.text }.valuesJoined()
    @JvmName("CollectionStringValueJoined")
    private fun Collection<String>.valuesJoined(): String = joinToString("$ASSIGN")
    private fun <T, F> writeQueries(x: Map<T, Collection<F>>, kMapper: (T) -> Any, vMapper: (F) -> String): String =
        x.map { (k, v) -> "$QUERY${kMapper(k)}$ASSIGN${v.map(vMapper).valuesJoined()}" }
            .joinToString("")

    companion object {

        private fun fromString(string: String, classes: List<Keyword>, page: Int, loc: Int, size: Int): Word {
            val split = string.split(DIVIDER)

            val header = split[0].split(ASSIGN)
            val id = header[0].toInt()
            val name = header[1]
            val tags = header.subList(2, header.size)
                .map { Keyword(it) }
                .toMutableSet()

            val attrs = readQueries(
                split[1],
                RelativeAttribute.Companion::fromString,
                { it },
            ).toMutableMap()

            val meanings = readQueries(
                split[2],
                { Keyword.from(classes, it.toInt()) },
                Meaning.Companion::fromString
            ).toMutableMap()

            return Word(id, name, tags, attrs, meanings, page, loc, size)
        }

        private fun fromFile(file: File, classes: List<Keyword>, page: Int): List<Word> {
            var caret = 0
            return file.bufferedReader().use { br ->
                br.readText().splitAndTrim(HEADER).map { s ->
                    val len = s.length
                    val word = fromString(s, classes, page, caret, len)
                    caret += len
                    word
                }
            }
        }

        fun fromDir(wrdDir: File, classes: List<Keyword>): List<Word> {
            var page = 0
            return wrdDir.listFiles()?.flatMap { f ->
                val words = fromFile(f, classes, page)
                page++
                words
            } ?: listOf()
        }

        private inline fun <T, F> readQueries(
            x: String,
            kMapper: (String) -> T?,
            vMapper: (String) -> F?,
        ): Map<T, MutableList<F>> =
            x.splitAndTrim(QUERY)
                .mapNotNull { entry ->
                    val split = entry.split(ASSIGN)
                    val key = kMapper(split[0]) ?: return@mapNotNull null
                    val value: MutableList<F> = split.subList(1, split.size)
                        .mapNotNull(vMapper)
                        .toMutableList()
                    key to value
                }
                .toMap()
    }
}

data class WordEditing(
    var newName: String? = null,
    var tagsEditing: (MutableSet<Keyword>.() -> Unit)? = null,
    var attrsEditing: (MutableMap<RelativeAttribute, MutableList<String>>.() -> Unit)? = null,
    var meaningsEditing: (MutableMap<Keyword, MutableList<Meaning>>.() -> Unit)? = null,
) {

    companion object {
        inline fun wordEditing(editing: WordEditing.() -> Unit): WordEditing =
            WordEditing().apply(editing)
    }
}

enum class RelativeAttribute(val symbol: Char) {
    Synonym('s'),
    Antonym('a'),
    Origin('o'),
    Derivative('d'),
    Related('r');

    val counter: RelativeAttribute by lazy {
        when (this) {
            Synonym -> Synonym
            Antonym -> Antonym
            Origin -> Derivative
            Derivative -> Origin
            Related -> Related
        }
    }

    /*lateinit var counter: org.shinytomato.convox.data.RelativeAttribute
        private set*/

    companion object {
        /*init {
            Synonym.counter = Synonym
            Antonym.counter = Antonym
            Origin.counter = Derivative
            Derivative.counter = Origin
            Related.counter = Related
        }*/

        private val map = entries.associateBy(RelativeAttribute::symbol)

        fun fromChar(value: Char): RelativeAttribute? {
            return map[value]
        }

        fun fromString(value: String): RelativeAttribute? {
            return map[value.first()]
        }
    }
}


class KeywordMap(list: MutableList<Keyword>) {

    fun indexOf(keyword: Keyword): Int? = map[keyword]

    private val map = list
        .mapIndexed { i, x -> x to i }
        .toMap()
}


data class Keyword(val text: String) {

    fun code(keywordMap: KeywordMap): Int? = keywordMap.indexOf(this)

    companion object {
        @JvmName("keywordFromListString")
        fun from(list: List<String>, code: Int): Keyword? =
            list.getOrNull(code)?.let { Keyword(it) }
        @JvmName("keywordFromListKeyword")
        fun from(list: List<Keyword>, code: Int): Keyword? =
            list.getOrNull(code)
    }
}

/*data class org.shinytomato.convox.data.Keyword(override val code: Int, val text: String): IKeyword {

    override fun toString(): String = "org.shinytomato.convox.data.Keyword($code:$text)"

    constructor(keywordMapping: List<String>, index: Int) : this(index, keywordMapping[index])

    companion object {
        fun fromList(x: List<String>): List<org.shinytomato.convox.data.Keyword> = List(x.size) { i -> org.shinytomato.convox.data.Keyword(x, i) }
    }
}*/


sealed class Meaning(protected val meaning: String) {
    abstract fun write(): String

    class MeaningOnly(meaning: String) : Meaning(meaning) {
        override fun toString(): String = "MeaningOnly(meaning=$meaning)"
        override fun write(): String = "$ASSIGN$meaning"
    }

    class DescribedMeaning(meaning: String, private val description: String) : Meaning(meaning) {
        override fun toString(): String = "DescribedMeaning(meaning=$meaning, description=$description)"
        override fun write(): String = "$ASSIGN$meaning$DESCRIBE$description"
    }

    companion object {
        fun fromString(input: String): Meaning =
            when (val index = input.indexOf(DESCRIBE)) {
                -1 -> MeaningOnly(input)
                else -> DescribedMeaning(
                    input.substring(0, index),
                    input.substring(index + 1, input.length)
                )
            }
    }
}