package org.shinytomato.convox.data


data class Keyword(val text: String) {

    fun code(keywordMap: KeywordMap): Int? = keywordMap.indexOf(this)

    companion object {
        @JvmName("keywordFromListString")
        fun from(list: List<String>, code: Int): Keyword? =
            list.getOrNull(code)?.let { Keyword(it) }
        @JvmName("keywordFromListKeyword")
        fun from(list: List<Keyword>, code: Int): Keyword? =
            list.getOrNull(code)
    }

}
class KeywordMap(list: MutableList<Keyword>) {

    fun indexOf(keyword: Keyword): Int? = map[keyword]

    private val map = list
        .mapIndexed { i, x -> x to i }
        .toMap()
}