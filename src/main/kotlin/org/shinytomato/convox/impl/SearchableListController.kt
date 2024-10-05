package org.shinytomato.convox.impl

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.geometry.Insets
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

    fun initInput(input: List<String>): Unit =
        initInput(FilteredList(FXCollections.observableList(input.sorted())))

    fun listPadding(padding: Double) {
        list.padding = Insets(padding)
        list.prefWidth = STAGE_WIDTH - (2 * (padding + 1.0))
    }

    private fun initInput(input: FilteredList<String>) {
        this.input = input
        if (::searchableListView.isInitialized)
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

    companion object {
        private const val STAGE_WIDTH = 250.0
    }
}