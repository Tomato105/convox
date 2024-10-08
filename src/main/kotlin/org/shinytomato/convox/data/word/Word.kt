package org.shinytomato.convox.data.word

import org.shinytomato.convox.data.Language
import org.shinytomato.convox.data.LanguageConfig
import org.shinytomato.convox.data.WritingConfig
import org.shinytomato.convox.data.splitAndTrim
import org.shinytomato.convox.toHashMap
import java.io.File


class Word(
    val id: Int,
    val name: String,
    private val tags: HashSet<Keyword>,
    private val attrs: HashMap<RelativeAttr, MutableList<WordRef>>,
    private val meanings: HashMap<Keyword, MutableList<Meaning>>,
    val page: Int,
    val loc: Int,
//    val size: Int,
) {

    private fun idHexDec(): String = id.toString(16)

    fun tags(): Set<Keyword> = tags.toSet()
    fun attrs(): Map<RelativeAttr, MutableList<WordRef>> = attrs.toMap()
    fun meanings(): Map<Keyword, MutableList<Meaning>> = meanings.toMap()

    fun registerTag(language: Language, tag: Keyword): Unit =
        language.edit(this) { tags.add(tag) }
    fun unregisterTag(language: Language, tag: Keyword): Unit =
        language.edit(this) { tags.remove(tag) }

    fun registerMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(this) { meanings[wordClass]?.add(meaning) }
    fun unregisterMeaning(language: Language, wordClass: Keyword, meaning: Meaning): Unit =
        language.edit(this) { meanings[wordClass]?.remove(meaning) }

    fun registerAttr(language: Language, attr: RelativeAttr, word: WordRef): Unit =
        language.edit(this) { attrs[attr]?.add(word) }

    fun editTags(language: Language, editing: HashSet<Keyword>.() -> Unit): Unit =
        language.edit(this) { tags.editing() }
    fun editAttrs(language: Language, editing: HashMap<RelativeAttr, MutableList<WordRef>>.() -> Unit) {
        language.edit(this) { attrs.editing() }
    }
    fun editMeanings(language: Language, editing: HashMap<Keyword, MutableList<Meaning>>.() -> Unit): Unit =
        language.edit(this) { meanings.editing() }

    fun write(config: WritingConfig): String {
        return "$HEADER${idHexDec()}" +
                "$ASSIGN$name" +
                "$ASSIGN${tags.valuesJoined()}" +
                "$DIVIDER" +
                writeQueries(attrs, RelativeAttr::symbol) { wordRef -> wordRef.idHexDec() } +
                "$DIVIDER" +
                writeQueries(meanings, { keyword -> keyword.codeHexDec(config.classes) }, Meaning::write)
    }

    @JvmName("CollectionKeywordValueJoined")
    private fun Collection<Keyword>.valuesJoined(): String = map { it.text }.valuesJoined()

    @JvmName("CollectionStringValueJoined")
    private fun Collection<String>.valuesJoined(): String = joinToString("$ASSIGN")
    private inline fun <T, F> writeQueries(
        x: Map<T, Collection<F>>,
        kMapper: (T) -> Any?,
        vMapper: (F) -> String?,
    ): String =
        x.map { (k, v) ->
            "$QUERY${kMapper(k)!!}" +
                    "$ASSIGN${v.mapNotNull(vMapper).valuesJoined()}"
        }
            .joinToString("")

    fun validateAttr(wordMap: HashMap<Int, Word>) {
        attrs.mapValues { (attr, refs) ->
            attrs[attr] = refs.map { wordRef ->
                wordRef.validateRef(wordMap)!!
            }.toMutableList()
        }
    }

    companion object {

        const val HEADER = '\u0001'
        const val DIVIDER = '\u0004'
        const val QUERY = '\u0005'
        const val ASSIGN = '\u0006'
        const val DESCRIBE = '\u0007'

        private fun fromString(
            string: String,
            languageConfig: LanguageConfig,
            page: Int,
            loc: Int,
            size: Int,
            wordMap: HashMap<Int, Word>,
        ): Word {
            val split = string.split(DIVIDER)

            val header = split[0].split(ASSIGN)
            val id = header[0].toInt(16)
            val name = header[1]
            val tags = header.subList(2, header.size)
                .map { languageConfig.tags[it.toInt(16)]!! }
                .toHashSet()

            val attrs = readQueries(
                split[1],
                RelativeAttr.Companion::fromString,
                { WordRef.IdRef(it.toInt(16)) as WordRef },
            ).toHashMap()

            val meanings = readQueries(
                split[2],
                { languageConfig.classes[it.toInt(16)] },
                Meaning.Companion::fromString
            ).toHashMap()

            return Word(id, name, tags, attrs, meanings, page, loc, /*size*/)
                .also { wordMap[id] = it }
        }

        private fun fromPage(
            file: File,
            languageConfig: LanguageConfig,
            page: Int,
            wordMap: HashMap<Int, Word>,
        ): List<Word> {
            var caret = 0
            return file.bufferedReader().use { br ->
                br.readText().splitAndTrim(HEADER).map { s ->
                    val len = s.length
                    val word = fromString(s, languageConfig, page, caret, len, wordMap)
                    caret += len
                    word
                }
            }
        }

        fun fromDir(wordDir: File, languageConfig: LanguageConfig): List<Word> {
            val wordMap = HashMap<Int, Word>()

            val words = wordDir.listFiles()?.flatMap { f ->
                val words = fromPage(f, languageConfig, f.nameWithoutExtension.toInt(16), wordMap)
                words
            }

            words?.forEach { it.validateAttr(wordMap) }
            return words ?: listOf()
        }

        private inline fun <T, F> readQueries(
            x: String,
            kMapper: (String) -> T?, // 빼고 싶을 때 null 반환
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
                }.toMap()
    }
}