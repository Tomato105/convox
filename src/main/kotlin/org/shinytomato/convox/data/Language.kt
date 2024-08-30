package org.shinytomato.convox.data

import java.io.File

data class Language(
    val config: LanguageConfig,
    val meanings: Map<String, List<WordContent>>,
) {
    fun saveLanguage(rootedDir: String) = saveLanguage(rootedDir.dictRooted())
    fun saveLanguage(dir: File) {
        dir.resolve("words").writer().buffered().use { br ->
            meanings.forEach { (wordName, wordContents) ->
                br.write("\u0001$wordName")
                wordContents.forEach { wordContent ->
                    wordContent.attr.forEach { (attrName, attrValue) ->
                        br.write("\u0002${attrName.code}\u0003$attrValue")
                    }
                    br.write(4)
                    wordContent.meanings.forEach { (wordClass, meanings) ->
                        br.write("\u0005${wordClass.code}")
                        meanings.forEach { meaning -> br.write(meaning.write()) }
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
    /*
    1 wordName
    2 attr 3 value
    2 attr 3 value
    4
        5 wordClass
            6 meaning
                7 example
            6 meaning
            6 meaning
        5 wordClass
            6 meaning
     */
}

data class WordContent(val attr: Map<Keyword, String>, val meanings: Map<Keyword, List<Meaning>>)

data class Keyword(val code: Int, val text: String) {
    override fun toString(): String = "Keyword(code=$code, text=$text)"

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