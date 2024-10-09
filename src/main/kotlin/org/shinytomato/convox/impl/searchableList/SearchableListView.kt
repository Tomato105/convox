package org.shinytomato.convox.impl.searchableList

import javafx.beans.value.ChangeListener
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

    //TODO: listener 말고 binding 못 쓰나? listview.itemsProperty().bind(...) 식으로
    // 그리고 다양성도 필요함.
    // graphic = 내부도 engine에서 해줘야. (최소한 지정 가능하게)
    // decorate도 지정할 수 있어야.
    private lateinit var listener: ChangeListener<String>
    private lateinit var engine: ListViewEngine<T>

    init {
        listview.cellFactory = Callback {
            object : ListCell<Displayable<T>>() {
                override fun updateItem(item: Displayable<T>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic =
                        if (empty || item == null) null
                        else if (queryField.text.isEmpty()) Text(item.label)
                        else decorate(item.label, item.label.indexOf(queryField.text), queryField.length)
                }
            }
        }
    }

    fun getSelected(): Displayable<T> = listview.selectionModel.selectedItem

    fun init(engine: ListViewEngine<T>) {
        this.engine = engine

        update("")

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

    companion object {
        private fun decorate(s: String, from: Int, length: Int): TextFlow {
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

interface ListViewEngine<T> {
    val source: MutableList<Displayable<T>>

    fun toLabel(item: T): String
    fun toDisplayed(item: T): Displayable<T> = object : Displayable<T> {
        override val label: String = toLabel(item)
        override val item: T = item
    }

    fun toDisplayedList(item: Collection<T>): List<Displayable<T>> = item.map(::toDisplayed)

    fun add(item: T): Boolean = source.add(toDisplayed(item))
    fun remove(item: Displayable<T>): Boolean = source.remove(item)
}

inline fun <T> simpleEngine(origin: Collection<T>, crossinline toLabel: (T) -> String): ListViewEngine<T> =
    object : ListViewEngine<T> {
        override val source: MutableList<Displayable<T>> = toDisplayedList(origin).toMutableList()

        override fun toLabel(item: T): String = toLabel(item)
    }

