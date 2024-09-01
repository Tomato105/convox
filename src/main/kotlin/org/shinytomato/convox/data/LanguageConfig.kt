package org.shinytomato.convox.data

import java.io.File

data class LanguageConfig(
    val attrNames: MutableList<String>,
    val wordClasses: MutableList<String>,
    val tagNames: MutableList<String>
) {

    fun saveConfig(rootedDir: String) = saveConfig(rootedDir.dictRooted())
    fun saveConfig(dir: File) {
        dir.resolve("config.config").writer().buffered().use { bw ->
            bw.write("attr:\n")
            attrNames.forEach { bw.write("\t$it\n") }
            bw.write("class:\n")
            wordClasses.forEach { bw.write("\t$it\n") }
            bw.write("tag:\n")
            tagNames.forEach { bw.write("\t$it\n") }
        }
    }

    fun loadLanguage(rootedDir: String): Language = loadLanguage(rootedDir.dictRooted())
    fun loadLanguage(dir: File): Language {

        fun String.splitTrimmed(delim: Char): List<String> =
            this.split(delim).run {
                if (first().isEmpty()) subList(1, this.size)
                else this
            }

        var caret = 0

        val words = dir.resolve("words.convox").reader().buffered().use { br ->
            br.readText()
                .splitTrimmed('\u0001')
                .map { word ->
                    caret++
                    val divided = word.splitTrimmed('\u0002')
                    val attrPart = divided[0].splitTrimmed('\u0005').toMutableList()
                    val meaningPart = divided[1]

                    val wordName = attrPart[0]

                    val attrs = attrPart.subList(1, attrPart.size).map { attrPair ->
                        val attrElements = attrPair.splitTrimmed('\u0006')
                        Attribute.from(Keyword(attrNames, attrElements[0].toInt()), attrElements.subList(1, attrElements.size))
                    }

                    val meanings = meaningPart.splitTrimmed('\u0005').associate { meaningPair ->
                        val meaningElements = meaningPair.splitTrimmed('\u0006')
                        val wordClassName = Keyword(wordClasses, meaningElements[0].toInt())

                        val meanings = meaningElements.subList(1, meaningElements.size)
                            .map(Meaning::read)
                            .toList()

                        wordClassName to meanings
                    }

                    val result = wordName to (WordContent(attrs, meanings) to caret)
                    caret += word.length
                    result
                }
                .groupBy { it.first }
                .mapValues { (_, values) -> values.map { ( _, word ) ->
                    LocatedWord(word.first, word.second)
                } }
        }


        return Language(this, words)
    }

    /*
    0   1   2   3   4   5   6   7   21
    NUL SOH STX ETX EOT ENQ ACK BEL NAK

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
    
    1 wordName
        5 attrCode 6 value
        5 attrCode 6 value
    2
        5 classCode
            6 meaning 7 example
            6 meaning
        5 classCode
            6 meaning 7 example
    2

    SOH wordName
        ENQ attrCode ACK value
        ENQ attrCode ACK value
    EOT
        ENQ classCode
            ACK meaning BEL example
            ACK meaning
        ENQ classCode
            ACK meaning BEL example
    EOT

     */

    companion object {
        fun loadConfig(rootedDir: String): LanguageConfig = loadConfig(rootedDir.dictRooted())
        fun loadConfig(dir: File): LanguageConfig {
            val attrNames: ArrayList<String> = arrayListOf()
            val wordClasses: ArrayList<String> = arrayListOf()
            val tagNames: ArrayList<String> = arrayListOf()
            var target: ArrayList<String>? = null

            dir.resolve("config.config")
                .also { if (!it.exists()) File("data/config.config").copyTo(it) }
                .reader()
                .buffered()
                .use { br ->
                    br.lines().map { it.trim() }.forEach { ln ->
                        if (ln.endsWith(':'))
                            target = when (ln.removeSuffix(":")) {
                                "attr" -> attrNames
                                "class" -> wordClasses
                                "tag" -> tagNames
                                else -> null
                            }
                        else
                            target?.add(ln)
                    }
                }
            return LanguageConfig(attrNames, wordClasses, tagNames)
        }
    }
}