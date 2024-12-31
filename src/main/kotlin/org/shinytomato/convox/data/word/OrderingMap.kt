package org.shinytomato.convox.data.word


class OrderingMap<T>(private val mapping: HashMap<Int, T>) {
    operator fun get(index: Int): T? = mapping[index]
    fun toWriting() = WritingOrderingMap(mapping.entries.associate { (i, x) -> x to i })
    override fun toString() = "OrderingMap${mapping.map {(k, v) -> "$k:$v"}}"

    fun put(item: T) {
        mapping.put(nextId, item)
        nextId++
    }

    private var nextId = mapping.keys.max() + 1
}

class WritingOrderingMap<T>(private val mapping: Map<T, Int>) {
    fun indexOf(x: T): Int? = mapping[x]
}