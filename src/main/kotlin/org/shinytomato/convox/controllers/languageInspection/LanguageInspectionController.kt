package org.shinytomato.convox.controllers.languageInspection

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.Parent
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class LanguageInspectionController : FXMLController() {

    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController

    @FXML
    fun initialize() {}

    fun initInput(languageName: String) {
        wordListViewController.initInput(Language.loadLanguage(languageName).meanings.keys.toList())
        wordListViewController.list.run {
            prefHeightProperty().bind(Bindings.size(items).multiply(20).add(1))
            fixedCellSize = 20.0
            maxHeight = 300.0
            prefWidth = 220.0
        }
    }

    // todo 할 일: list.fxml이 SearchableList를 Controller로 두고,
    //  그걸 포함하는 컨트롤러(LanguageInspectionController)등에서 initInput하는 방식으로

    companion object : Loadable<LanguageInspectionController>("languageInspection/main")
}