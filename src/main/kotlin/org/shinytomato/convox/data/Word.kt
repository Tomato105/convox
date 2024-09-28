package org.shinytomato.convox.data

import java.io.File


class Word(
    val id: Int,
    name: String,
    private val tags: MutableSet<Keyword>,
    private val attrs: MutableMap<RelativeAttr, MutableList<String>>,
    private val meanings: MutableMap<Keyword, MutableList<Meaning>>,
    val page: Int,
    val loc: Int,
    val size: Int,
) {
    var name = name
        private set


    fun tags(): Set<Keyword> = tags.toSet()
    fun attrs(): Map<RelativeAttr, MutableList<String>> = attrs.toMap()
    fun meanings(): Map<Keyword, MutableList<Meaning>> = meanings.toMap()

    fun registerTag(language: Language, tag: Keyword): Unit =
        language.edit(this) { tags.add(tag) }
    fun unregisterTag(language: Language, tag: Keyword): Unit =
        language.edit(this) { tags.remove(tag) }

    fun registerMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(this) { meanings[wordClass]?.add(meaning) }
    fun unregisterMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(this) { meanings[wordClass]?.remove(meaning) }

    /*fun registerAttr(language: Language, attr: RelativeAttr, meaning: Meaning): Unit
            = language.edit(this) { attrs[attr]?.add(meaning) }*/

    fun editTags(language: Language, editing: MutableSet<Keyword>.() -> Unit): Unit =
        language.edit(this) { tags.editing() }
    fun editAttrs(language: Language, editing: MutableMap<RelativeAttr, MutableList<String>>.() -> Unit) {
        language.edit(this) { attrs.editing() }
        // TODO 상대방에게도 추가
    }
    fun editMeanings(language: Language, editing: MutableMap<Keyword, MutableList<Meaning>>.() -> Unit): Unit =
        language.edit(this) { meanings.editing() }

    fun write(wordClassMapping: KeywordMap): String {
        return "$HEADER$id" +
                "$ASSIGN$name" +
                "$ASSIGN${tags.valuesJoined()}" +
                "$DIVIDER" +
                writeQueries(attrs, RelativeAttr::symbol) { it } +
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

        const val HEADER = '\u0001'
        const val DIVIDER = '\u0004'
        const val QUERY = '\u0005'
        const val ASSIGN = '\u0006'
        const val DESCRIBE = '\u0007'

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
                RelativeAttr.Companion::fromString,
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