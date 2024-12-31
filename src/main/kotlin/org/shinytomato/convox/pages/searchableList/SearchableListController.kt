package org.shinytomato.convox.pages.searchableList

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import org.shinytomato.convox.impl.SelectionReceiver
import org.shinytomato.convox.impl.SelectionGiver
import org.shinytomato.convox.pages.searchableList.ListViewEngine.Companion.simpleEngine

class SearchableListController<T>() : SelectionGiver<Pair<T, MouseEvent>> {

    @FXML private lateinit var queryField: TextField
    @FXML lateinit var listview: ListView<Displayable<T>>

    var selectionReceiver: SelectionReceiver<Pair<T, MouseEvent>>? = null
    private lateinit var searchableListView: SearchableListView<T>

    @FXML
    fun initialize() {
        searchableListView = SearchableListView(listview, queryField)
    }

    fun initEngine(engine: ListViewEngine<T>): Unit = searchableListView.initEngine(engine)
    inline fun initSimpleEngine(origin: Collection<T>, crossinline toDisplay: (T) -> String) =
        initEngine(simpleEngine(origin, toDisplay))

    fun listPadding(padding: Double) {
        listview.padding = Insets(padding)
        listview.prefWidth = STAGE_WIDTH - (2 * (padding + 1.0))
    }

    fun itemSelected(event: MouseEvent) {
        giveSelection(
            Pair(
                searchableListView.selectedItem()?.item ?: return,
                event
            )
        )
    }

    override fun giveSelection(selected: Pair<T, MouseEvent>): Unit? =
        selectionReceiver?.receiveSelection(selected)

    companion object {
        private const val STAGE_WIDTH = 250.0
    }
}