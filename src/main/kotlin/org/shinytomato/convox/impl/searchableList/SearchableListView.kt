package org.shinytomato.convox.impl.searchableList

import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.util.Callback

interface Displayable<T> {
    val label: String
    val item: T
}

class SearchableListView<T>(
    private val listview: ListView<Displayable<T>>,
    private val queryField: TextField,
) {
    private lateinit var listener: ChangeListener<String>
    private lateinit var engine: ListViewEngine<T>

    init {
        listview.cellFactory = Callback {
            object : ListCell<Displayable<T>>() {
                override fun updateItem(item: Displayable<T>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic =
                        if (empty || item == null) null
                        else engine.toGraphic(item, queryField.text)
                }
            }
        }
    }

    fun getSelected(): Displayable<T> = listview.selectionModel.selectedItem

    fun init(engine: ListViewEngine<T>) {
        this.engine = engine

        update(queryField.text)

        queryField.textProperty().let { textProp ->
            if (::listener.isInitialized)
                textProp.removeListener(listener)
            textProp.addListener(getListener())
        }
    }

    fun update(query: String) {
        listview.items.setAll(
            engine.source.filter { it.label.contains(query) }
        )
    }

    private fun getListener(): ChangeListener<String> =
        ChangeListener { _, _, query ->
            update(query)
        }
}

abstract class ListViewEngine<T>(source: Collection<T>) {

    private val _source = toDisplayableList(source).toMutableList()

    val source
        get() = _source as List<Displayable<T>>

    // String 값을 산출하는 함수
    abstract fun toLabel(item: T): String

    // 화면에 표시되는 Node
    open fun toGraphic(item: Displayable<T>, query: String): Node =
        if (query.isEmpty()) Text(item.label)
        else partialBold(item.label, item.label.indexOf(query), query.length)

    // 항목을 추가할 때 필요
    private fun toDisplayable(item: T): Displayable<T> = object : Displayable<T> {
        override val label: String
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

inline fun <T> simpleEngine(origin: Collection<T>, crossinline toLabel: (T) -> String): ListViewEngine<T> =
    object : ListViewEngine<T>(origin) {
        override fun toLabel(item: T): String = toLabel(item)
    }

