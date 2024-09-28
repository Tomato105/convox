package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.shinytomato.convox.data.ResourceManager
import org.shinytomato.convox.data.ResourceManager.resolveResourcePath
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class LanguageInspectionController : FXMLController() {

    @FXML lateinit var editorialModeButton: Button
    @FXML lateinit var editorialModeButtonImage: ImageView
    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController
    private lateinit var languageName: String

    @FXML
    fun initialize() {
        editorialModeButtonImage.imageProperty()
            .bind(Bindings.`when`(isEditing).then(viewingImage).otherwise(editingImage))
        editorialModeButton.setOnMouseClicked { _ ->
            isEditing.set(isEditing.not().get())
        }
    }

    fun initInput(languageName: String) {
        this.languageName = languageName

        wordListViewController.run {
            initInput(Language.fromDir(ResourceManager.dictDir.resolve(languageName)).words().keys.toList())

            list.prefHeightProperty().bind(Bindings.size(list.items).multiply(CELL_SIZE).add(1 + LIST_PADDING * 2))
            list.padding = Insets(LIST_PADDING, LIST_PADDING, LIST_PADDING, LIST_PADDING)
            list.fixedCellSize = CELL_SIZE
        }
    }

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = languageName
            width = STAGE_WIDTH
            height = STAGE_HEIGHT
            isResizable = true
        }
    }

    companion object : Loadable<LanguageInspectionController>("languageInspection") {
        const val LIST_PADDING = 10.0
        const val STAGE_HEIGHT = 400.0
        const val STAGE_WIDTH = 500.0
        const val CELL_SIZE = 25.0

        private val isEditing = SimpleBooleanProperty(false)
        private val viewingImage = Image(resolveResourcePath("../image/eye.png"))
        private val editingImage = Image(resolveResourcePath("../image/pencil.png"))
    }
}