package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxApplication.ApplicationState.getResource
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class LanguageInspectionController : FXMLController() {

    @FXML lateinit var editorialModeButtonImage: ImageView
    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController
    private lateinit var languageName: String

    @FXML
    fun initialize() {
        editorialModeButtonImage.imageProperty().bind(Bindings.`when`(isEditing).then(viewingImage).otherwise(editingImage))
    }

    fun initInput(languageName: String) {
        this.languageName = languageName

        wordListViewController.run {
//            initInput(LanguageLegacy.loadLanguageLegacy(languageName).words.keys.toList())

            list.prefHeightProperty().bind(Bindings.size(list.items).multiply(25).add(21))
            list.padding = Insets(LIST_INSET, LIST_INSET, LIST_INSET, LIST_INSET)
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

    companion object : Loadable<LanguageInspectionController>("languageInspection") {
        const val LIST_INSET = 10.0
        private val isEditing = SimpleBooleanProperty(false)
        private val viewingImage = Image("image/eye.png".getResource().toString())
        private val editingImage = Image("image/pencil.png".getResource().toString())
    }
}