package org.shinytomato.convox.data.word


class Keyword(val text: String) {

    fun code(mapping: WritingOrderingMap<Keyword>): Int? = mapping.indexOf(this)
    fun codeHexDec(mapping: WritingOrderingMap<Keyword>): String? = code(mapping)?.toString(16)

    override fun toString() = text
}