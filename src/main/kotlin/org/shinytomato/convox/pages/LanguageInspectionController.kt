package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class LanguageInspectionController : FXMLController() {

    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController
    lateinit var languageName: String

    @FXML
    fun initialize() {}

    fun initInput(languageName: String) {
        this.languageName = languageName

        wordListViewController.run {
            initInput(Language.loadLanguage(languageName).meanings.keys.toList())

            list.prefHeightProperty().bind(Bindings.size(list.items).multiply(25).add(21))
            list.padding = Insets(10.0, 10.0, 10.0, 10.0)
            list.fixedCellSize = 25.0
        }
    }

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = languageName
            width = 500.0
            height = 400.0
            isResizable = true
            minWidth = 200.0
            minHeight = 100.0
        }
    }

    companion object : Loadable<LanguageInspectionController>("languageInspection")
}