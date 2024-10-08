package org.shinytomato.convox.data.word


class OrderingMap<T>(private val mapping: HashMap<Int, T>) {
    operator fun get(index: Int): T? = mapping[index]
    fun toWriting() = WritingOrderingMap(mapping.entries.associate { (i, x) -> x to i })
}

class WritingOrderingMap<T>(private val mapping: Map<T, Int>) {
    fun indexOf(x: T): Int? = mapping[x]
}