package org.shinytomato.convox.data



enum class RelativeAttr(val symbol: Char) {
    Synonym('s'),
    Antonym('a'),
    Origin('o'),
    Derivative('d'),
    Related('r');

    val counter: RelativeAttr by lazy {
        when (this) {
            Synonym -> Synonym
            Antonym -> Antonym
            Origin -> Derivative
            Derivative -> Origin
            Related -> Related
        }
    }

    companion object {

        private val map = entries.associateBy(RelativeAttr::symbol)

        fun fromChar(value: Char): RelativeAttr? {
            return map[value]
        }

        fun fromString(value: String): RelativeAttr? {
            return map[value.first()]
        }
    }
}