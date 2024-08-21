package org.shinytomato.convox.data

import java.io.File

object DataManager {
    val root = File("data/dict/")
    fun loadLanguageList(): List<String> =
        File("data/index").reader().buffered().use { br ->
            br.lines().map { it.trim() }.filter { root.resolve(it).exists() }.toList()
        }
}

data class LanguageConfig(
    val attrNames: MutableList<String>,
    val wordClasses: MutableList<String>,
) {

    fun saveConfig(dir: File) {
        dir.resolve("config").writer().buffered().use { bw ->
            bw.write("attr:\n")
            attrNames.forEach { bw.write("$it\n") }
            bw.write("class:\n")
            wordClasses.forEach { bw.write("$it\n") }
        }
    }

    fun loadLanguage(lang: File): Language {

        fun String.splitTrimmed(delim: Char) =
            this.split(delim.also { println("delim: ${it.code}") }).also(::println).run {
                if (first().isEmpty()) subList(1, this.size)
                else this
            }

        val words = lang.resolve("words").reader().buffered().use { br ->
            br.readText()
                .splitTrimmed('\u0001')
                .map { word ->
                    val divided = word.splitTrimmed('\u0003')
                    val attrPart = divided[0].splitTrimmed('\u0002').toMutableList()
                    val meaningPart = divided[1]

                    val wordName = attrPart[0]

                    val attrs = attrPart.subList(1, attrPart.size).associate { attrPair ->
                        val attrElements = attrPair.splitTrimmed('\u0000')
                        Keyword(attrNames, attrElements[0].toInt()) to attrElements[1]
                    }

                    val meanings = meaningPart.splitTrimmed('\u0005').associate { meaningPair ->
                        val meaningElements = meaningPair.splitTrimmed('\u0006')
                        val wordClassName = Keyword(wordClasses, meaningElements[0].toInt())

                        val meanings = meaningElements.subList(1, meaningElements.size)
                            .map(Meaning::read)
                            .toList()

                        wordClassName to meanings
                    }

                    wordName to WordContent(attrs, meanings)
                }
                .groupBy { it.first }
                .mapValues { (_, values) -> values.map { it.second } }
        }


        return Language(this, words)
    }

    /*
    0   1   2   3   4   5   6   7   21
    NUL SOH STX ETX EOT ENQ ACK BEL NAK

    1 wordName
    2 attr 0 value
    2 attr 0 value
    3
        5 wordClass
            6 meaning
                7 example
            6 meaning
            6 meaning
        5 wordClass
            6 meaning
     */

    companion object {
        fun loadConfig(dir: File): LanguageConfig {
            val attrNames: ArrayList<String> = arrayListOf()
            val wordClasses: ArrayList<String> = arrayListOf()
            var target: ArrayList<String>? = null

            dir.resolve("config")
                .also { if (!it.exists()) DataManager.root.resolve("config").copyTo(it) }
                .reader()
                .buffered()
                .use { br ->
                    br.lines().map { it.trim() }.forEach { ln ->
                        if (ln.endsWith(':'))
                            target = when (ln.removeSuffix(":")) {
                                "attr" -> attrNames
                                "class" -> wordClasses
                                else -> null
                            }
                        else
                            target?.add(ln)
                    }
                }
            return LanguageConfig(attrNames, wordClasses).also(::println)
        }
    }
}

data class Language(
    val config: LanguageConfig,
    val meanings: Map<String, List<WordContent>>,
) {

    fun saveLanguage(dir: File) {
        dir.resolve("words").writer().buffered().use { br ->
            meanings.forEach { (wordName, wordContents) ->
                br.write("\u0001$wordName")
                wordContents.forEach { wordContent ->
                    wordContent.attr.forEach { (attrName, attrValue) ->
                        br.write("\u0002${attrName.code}\u0000$attrValue")
                    }
                    br.write(3)
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
        fun loadLanguage(dir: File): Language = LanguageConfig.loadConfig(dir).loadLanguage(dir)
    }
    /*
    1 wordName
    2 attr 0 value
    2 attr 0 value
    3
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


/*


SOH sen NUL
STX p NUL send ETX
STX o NUL 영어 send ETX
STX m NUL
    ENQ v NUL
        ACK 말하다
            BEL 예문 NUL
    NAK
ETX
EOT
 */

/*sealed class WordClass(val code: Int, val abbreviation: Set<String>) {
    // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
    data object Adjective: WordClass(0, setOf("adjective", "adj", "형용사", "형"))
    data object Adverb: WordClass(setOf("adverb", "adv", "부사", "부"))
    data object Conjunction: WordClass(setOf("conjunction", "conj", "cnj", "conjun", "접속사", "접"))
    data object Determiner: WordClass(setOf("determiner", "det", "d", "한정사", "한", "관형사", "관"))
    data object Interjection: WordClass(setOf("interjection", "interj", "intrj", "intj", "inj", "inter", "intr", "int", "감탄사", "감"))
    data object Noun: WordClass(setOf("noun", "n", "명사", "명"))
    data object Numeral: WordClass(setOf("numeral", "num", "수사", "수"))
    data object Particle: WordClass(setOf("particle", "postposition", "postp", "post", "조사", "조", "후치사", "후"))
    data object Preposition: WordClass(setOf("preposition", "prep", "전치사", "전"))
    data object Pronoun: WordClass(setOf("pronoun", "pron", "대명사", "대"))
    data object Verb: WordClass(setOf("verb", "v", "동사", "동"))

    class CustomClass(alternatives: Set<String>): WordClass(alternatives)
}*/
