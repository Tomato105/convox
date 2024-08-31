package org.shinytomato.convox.impl

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import java.util.function.Predicate

class SearchableListView(
    private val listview: ListView<TextFlow>,
    private val filteredList: FilteredList<String>,
    private val searchedBy: TextField,
) {

    private val listener = ChangeListener { value, old, new -> updateList(new) }

    init {
        updateList("")
        searchedBy.textProperty().addListener(listener)
    }

    fun close() {
        searchedBy.textProperty().removeListener(listener)
    }

    private fun updateList(text: String) {
        listview.items.setAll(
            if (text.isEmpty()) {
                filteredList.predicate = Predicate { true }
                filteredList.map { TextFlow(Text(it)) }
            } else {
                filteredList.predicate = Predicate { it.contains(text, ignoreCase = true) }
                filteredList.map {
                    val index = it.indexOf(text)
                    val point = index + text.length
                    TextFlow(
                        Text(it.substring(0..<index)),
                        Text(it.substring(index..<point))
                            .apply { style = "-fx-font-weight: bold" },
                        Text(it.substring(point..<it.length)),
                    )
                }
            }
        )
    }

    constructor(listview: ListView<TextFlow>, list: List<String>, searchedBy: TextField) : this(
        listview,
        FilteredList(FXCollections.observableList(list)),
        searchedBy
    )
}