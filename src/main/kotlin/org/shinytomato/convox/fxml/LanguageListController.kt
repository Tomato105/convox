package org.shinytomato.convox.fxml

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.i.FXMLController
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.i.IGetSelected
import org.shinytomato.convox.i.Loadable

class LanguageListController: FXMLController() {

    override fun whenLoad() {}

    @FXML private lateinit var languageList: ListView<String>

    internal var getSelected: IGetSelected? = null

    @FXML
    private fun initialize() {
        languageList.run {
            items.addAll(DataManager.loadLanguageList())
            fixedCellSize = 40.0

            prefHeightProperty().bind(Bindings.size(languageList.items).multiply(40))
        }
    }

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(languageList.selectionModel.selectedItem, event.clickCount)
    }

    fun openCurrentlySelected() {
        val selected = languageList.selectionModel.selectedItem.also(::println) ?: return
        ConvoxAction.languageStructure(stage, selected)
    }

    companion object: Loadable("languageList")
}