package org.shinytomato.convox.fxml

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.i.FXMLController
import org.shinytomato.convox.i.IGetSelected
import org.shinytomato.convox.i.Loadable
import org.shinytomato.convox.i.SearchableListView

class LanguageListController : FXMLController() {

    @FXML
    private lateinit var search: TextField

    @FXML
    private lateinit var list: ListView<TextFlow>

    // 외부에서 getSelected에 자기를 등록해야 함.
    internal var getSelected: IGetSelected<TextFlow>? = null

    lateinit var searchableListView: SearchableListView


    @FXML
    private fun initialize() {

        list.prefHeightProperty().bind(Bindings.size(list.items).multiply(38).add(1))
        searchableListView = SearchableListView(list, FXCollections.observableList(DataManager.loadLanguageList()), search)
    }

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            list.selectionModel.selectedItem,
            event
        )
    }

    fun openCurrentlySelected() {
        val selected = list.selectionModel.selectedItem ?: return
        ConvoxAction.languageStructure(selected.children.joinToString(separator = "") { (it as Text).text })
    }

    companion object : Loadable("languageList")
}