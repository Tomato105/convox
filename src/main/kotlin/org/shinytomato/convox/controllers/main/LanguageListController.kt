package org.shinytomato.convox.controllers.main

import javafx.beans.binding.Bindings
import javafx.scene.text.Text
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.i.*

class LanguageListController : SearchableListController(DataManager.loadLanguageList()) {

    override fun action() { list.prefHeightProperty().bind(Bindings.size(list.items).multiply(38).add(1)) }

    fun openCurrentlySelected() {
        val selected = list.selectionModel.selectedItem ?: return
        ConvoxAction.languageStructure(selected.children.joinToString(separator = "") { (it as Text).text })
    }

    companion object : Loadable("main/languageList")
}