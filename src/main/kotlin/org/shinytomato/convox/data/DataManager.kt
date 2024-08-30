package org.shinytomato.convox.data

import org.shinytomato.convox.data.DataManager.dictRoot
import java.io.File

object DataManager {
    val dictRoot = "data/dict"
    fun loadLanguageList(): List<String> =
        File("data/index.dat").reader().buffered().use { br ->
            br.lines().map { it.trim() }.filter { it.dictRooted().exists() }.toList()
        }
}

fun String.dictRooted(): File = File("$dictRoot/$this")
fun String.dictRootedString(): String = "$dictRoot/$this"
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
