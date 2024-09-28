package org.shinytomato.convox.impl

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.text.TextFlow

open class SearchableListController() : FXMLController() {
    private var input: FilteredList<String> = FilteredList(FXCollections.observableList(listOf("Input was not initialized")))

    @FXML lateinit var container: ScrollPane
    @FXML private lateinit var search: TextField
    @FXML lateinit var list: ListView<TextFlow>

    internal var getSelected: IGetSelected<TextFlow>? = null
    private lateinit var searchableListView: SearchableListView

    fun initInput(input: List<String>) {
        searchableListView = SearchableListView(list, input, search)
        initInput(FilteredList(FXCollections.observableList(input)))
    }

    fun initInput(input: FilteredList<String>) {
        this.input = input
        searchableListView.close()
        searchableListView = SearchableListView(list, input, search)
    }

    constructor(input: List<String>) : this(FilteredList(FXCollections.observableList(input)))
    constructor(input: FilteredList<String>) : this() { this.input = input }


    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            list.selectionModel.selectedItem,
            event
        )
    }
}