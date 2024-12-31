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
    val pronunciation: String?,
    private val tags: HashSet<Keyword>,
    private val attrs: HashMap<RelativeAttr, MutableList<WordRef>>,
    private val meanings: HashMap<Keyword, MutableList<Meaning>>,
    val page: Int,
    val loc: Int,
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

    @JvmName("CollectionAnyValueJoined")
    private fun Collection<Any>.valuesJoined(): String = joinToString(transform = Any::toString)

    private inline fun <T, F> writeQueries(
        x: Map<T, Collection<F>>,
        kMapper: (T) -> Any?,
        vMapper: (F) -> Any?,
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

        const val HEADER = '\u0001' // SOH
        const val DIVIDER = '\u0004' // EOT
        const val QUERY = '\u0005' // ENQ
        const val ASSIGN = '\u0006' // ACK
        const val DESCRIBE = '\u0007' // BEL

        private fun fromString(
            string: String,
            languageConfig: LanguageConfig,
            page: Int,
            loc: Int,
            wordMap: HashMap<Int, Word>,
        ): Word {

            val split = string.split(DIVIDER)

            val header = split[0].split(ASSIGN)
            val id = header[0].toInt(16)

            val main = header[1].split(DESCRIBE)
            val name = main[0]
            val pronunciation = main.getOrNull(1)

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
                { languageConfig.classes[id.toInt()] },
                Meaning.Companion::fromString,
            ).toHashMap()

            return Word(id, name, pronunciation ,tags, attrs, meanings, page, loc)
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
                br.readText().splitAndTrim(HEADER).mapNotNull { s ->
                    val len = s.length

                    // 숫자 파싱에서 오류나면 그냥 버리기
                    val word = try {
                        fromString(s, languageConfig, page, len, wordMap)
                    } catch (ex: NumberFormatException) {
                        ex.printStackTrace()
                        null
                    }

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

        // kMapper 결과가 null이면 value에 예외가 있어도 그 전에 넘겨서 예외가 안생기는 경우 有
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