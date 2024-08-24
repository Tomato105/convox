package org.shinytomato.convox.i

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.text.TextFlow

open class SearchableListController(val input: FilteredList<String>) : FXMLController() {
    @FXML private lateinit var search: TextField

    @FXML protected lateinit var list: ListView<TextFlow>

    internal var getSelected: IGetSelected<TextFlow>? = null
    private lateinit var searchableListView: SearchableListView

    @FXML
    private fun initialize() {
        searchableListView = SearchableListView(list, input, search)
        this.action()
    }

    constructor(input: List<String>) : this(FilteredList(FXCollections.observableList(input)))

    protected open fun action() {}

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            list.selectionModel.selectedItem,
            event
        )
    }
}