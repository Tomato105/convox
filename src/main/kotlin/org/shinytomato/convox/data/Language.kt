package org.shinytomato.convox.data

import java.io.File

data class Language(
    val config: LanguageConfig,
    val meanings: Map<String, List<LocatedWord>>,
) {
    fun saveLanguage(rootedDir: String) = saveLanguage(rootedDir.dictRooted())
    fun saveLanguage(dir: File) {
        dir.resolve("words").writer().buffered().use { bw ->
            meanings.forEach { (wordName, locatedWords) ->
                bw.write("\u0001$wordName")
                locatedWords.forEach { locatedWord ->
                    locatedWord.content.attrs.forEach { attr ->
                        bw.write("\u0005${attr.keyword.code}\u0006${attr.write()}")
                    }
                    bw.write(2)
                    locatedWord.content.meanings.forEach { (wordClass, meanings) ->
                        bw.write("\u0005${wordClass.code}")
                        meanings.forEach { meaning -> bw.write(meaning.write()) }
                    }
                }
            }
        }
        config.saveConfig(dir)
    }

    companion object {
        fun loadLanguage(rootedDir: String): Language = loadLanguage(rootedDir.dictRooted())
        fun loadLanguage(dir: File): Language = LanguageConfig.loadConfig(dir).loadLanguage(dir)
    }
}

data class LocatedWord(val content: WordContent, val loc: Int)

data class WordContent(val attrs: List<Attribute>, val meanings: Map<Keyword, List<Meaning>>)

sealed class Attribute(val keyword: Keyword) {
    abstract fun write(): String

    class EmptyAttribute(keyword: Keyword) : Attribute(keyword) {
        override fun write(): String = "\u0005${keyword.code}"
    }

    class SimpleAttribute(keyword: Keyword, val value: String) : Attribute(keyword) {
        override fun write(): String = "\u0005${keyword.code}\u0006${value}"
    }

    class ListAttribute(keyword: Keyword, val values: List<String>) : Attribute(keyword) {
        override fun write(): String {
            val valuePart = values.fold(StringBuilder()) { acc, s -> acc.append("\u0006$s") }
            return "\u0005${keyword.code}\u0006$valuePart"
        }
    }

    companion object {
        fun from(keyword: Keyword, x: List<String>): Attribute {
            return when (x.size) {
                0 -> EmptyAttribute(keyword)
                1 -> SimpleAttribute(keyword, x.first())
                else -> ListAttribute(keyword, x)
            }
        }
    }
}

data class Keyword(val code: Int, val text: String) {

    override fun toString(): String = "Keyword($code:$text)"

    constructor(from: List<String>, index: Int) : this(index, from[index])
}


sealed class Meaning(protected val meaning: String) {
    abstract fun write(): String

    class MeaningOnly(meaning: String) : Meaning(meaning) {
        override fun toString(): String = "MeaningOnly(meaning=$meaning)"
        override fun write(): String = "\u0006$meaning"
    }

    class DescribedMeaning(meaning: String, private val description: String) : Meaning(meaning) {
        override fun toString(): String = "DescribedMeaning(meaning=$meaning, description=$description)"
        override fun write(): String = "\u0006$meaning\u0007$description"
    }

    companion object {
        fun read(input: String): Meaning =
            when (val index = input.indexOf('\u0007')) {
                -1 -> MeaningOnly(input)
                else -> DescribedMeaning(
                    input.substring(0, index),
                    input.substring(index + 1, input.length)
                )
            }
    }
}