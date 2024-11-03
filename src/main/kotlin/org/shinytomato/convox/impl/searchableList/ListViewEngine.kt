package org.shinytomato.convox.impl.searchableList

import javafx.scene.Node
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

abstract class ListViewEngine<T>(source: Collection<T>) {

    private val _source = toDisplayableList(source).toMutableList()

    val source
        get() = _source as List<Displayable<T>>

    // String 값을 산출하는 함수
    abstract fun toLabel(item: T): String

    // 화면에 표시되는 Node
    open fun toGraphic(item: Displayable<T>, query: String): Node =
        if (query.isEmpty()) Text(item.display)
        else partialBold(item.display, item.display.indexOf(query), query.length)

    // 항목을 추가할 때 필요
    private fun toDisplayable(item: T): Displayable<T> = object : Displayable<T> {
        override val display: String
            get() = toLabel(item)
        override val item: T = item
    }
    private fun toDisplayableList(item: Collection<T>): List<Displayable<T>> = item.map(::toDisplayable)

    fun add(item: T): Boolean = _source.add(toDisplayable(item))
    fun remove(item: Displayable<T>): Boolean = _source.remove(item)

    companion object {
        fun partialBold(s: String, from: Int, length: Int): TextFlow {
            val until = from + length
            return TextFlow(
                Text(s.substring(0..<from)),
                Text(s.substring(from..<until))
                    .apply { style = "-fx-font-weight: bold" },
                Text(s.substring(until..<s.length)),
            )
        }
    }
}

inline fun <T> simpleEngine(origin: Collection<T>, crossinline toDisplay: (T) -> String): ListViewEngine<T> =
    object : ListViewEngine<T>(origin) {
        override fun toLabel(item: T): String = toDisplay(item)
    }