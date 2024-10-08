package org.shinytomato.convox.impl.searchableList

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected

open class SearchableListController<T>() : FXMLController() {

    @FXML private lateinit var queryField: TextField
    @FXML lateinit var listview: ListView<Displayable<T>>

    internal var getSelected: IGetSelected<T>? = null
    private lateinit var searchableList: SearchableListView<T>

    @FXML
    fun initialize() {
        searchableList = SearchableListView(listview, queryField)
    }

    fun initOrigin(engine: ListViewEngine<T>) {
        searchableList.init(engine)
    }

    fun listPadding(padding: Double) {
        listview.padding = Insets(padding)
        listview.prefWidth = STAGE_WIDTH - (2 * (padding + 1.0))
    }

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            searchableList.getSelected().item,
            event
        )
    }

    companion object {
        private const val STAGE_WIDTH = 250.0
    }
}