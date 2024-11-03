package org.shinytomato.convox.impl.searchableList

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected
import org.shinytomato.convox.impl.ISelector

open class SearchableListController<T>() : FXMLController(), ISelector<T> {

    @FXML private lateinit var queryField: TextField
    @FXML lateinit var listview: ListView<Displayable<T>>

    override var getSelected: IGetSelected<T>? = null
    private lateinit var searchableList: SearchableListView<T>

    @FXML
    fun initialize() {
        searchableList = SearchableListView(listview, queryField)
    }

    fun initEngine(engine: ListViewEngine<T>) {
        searchableList.initEngine(engine)
    }

    fun listPadding(padding: Double) {
        listview.padding = Insets(padding)
        listview.prefWidth = STAGE_WIDTH - (2 * (padding + 1.0))
    }

    override fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            searchableList.getSelected().item,
            event
        )
    }

    companion object {
        private const val STAGE_WIDTH = 250.0
    }
}