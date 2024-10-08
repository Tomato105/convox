package org.shinytomato.convox.data.word

import org.shinytomato.convox.data.word.Word.Companion.ASSIGN
import org.shinytomato.convox.data.word.Word.Companion.DESCRIBE


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