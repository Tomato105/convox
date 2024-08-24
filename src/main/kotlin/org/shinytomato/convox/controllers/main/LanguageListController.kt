package org.shinytomato.convox.controllers.main

import javafx.beans.binding.Bindings
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.i.*

class LanguageListController : SearchableListController(DataManager.loadLanguageList()) {

    override fun action() { list.prefHeightProperty().bind(Bindings.size(list.items).multiply(38).add(1)) }

    companion object : Loadable("main/languageList")
}