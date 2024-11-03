package org.shinytomato.convox.impl.searchableList

import javafx.beans.value.ChangeListener
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.util.Callback

interface Displayable<T> {
    val display: String
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

    fun initEngine(engine: ListViewEngine<T>) {
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
            engine.source.filter { it.display.contains(query) }
        )
    }

    private fun getListener(): ChangeListener<String> =
        ChangeListener { _, _, query ->
            update(query)
        }
}