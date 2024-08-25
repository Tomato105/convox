package org.shinytomato.convox.impl

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.text.TextFlow
import org.shinytomato.convox.ConvoxApplication.ApplicationState.unintendedBehavior

open class SearchableListController() : FXMLController() {
    private var input: FilteredList<String> =
    FilteredList(FXCollections.observableList(listOf(unintendedBehavior("iL02", "SearchableListController.input is not initialized"))))

    @FXML private lateinit var search: TextField

    @FXML lateinit var list: ListView<TextFlow>

    internal var getSelected: IGetSelected<TextFlow>? = null
    private lateinit var searchableListView: SearchableListView

    @FXML
    private fun initialize() {
        searchableListView = SearchableListView(list, input, search)
        action()
    }

    fun initInput(input: List<String>) {
        initInput(FilteredList(FXCollections.observableList(input)))
    }

    fun initInput(input: FilteredList<String>) {
        this.input = input
        searchableListView = SearchableListView(list, input, search)
    }

    constructor(input: List<String>) : this(FilteredList(FXCollections.observableList(input)))
    constructor(input: FilteredList<String>) : this() { this.input = input }

    protected open fun action() {}

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            list.selectionModel.selectedItem,
            event
        )
    }
}