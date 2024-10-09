package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.shinytomato.convox.data.ResourceManager.resolveResourcePath
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.searchableList.SearchableListController
import org.shinytomato.convox.impl.searchableList.simpleEngine
import java.io.File

class LanguageInspectionController : FXMLController() {

    @FXML lateinit var editorialModeButton: Button
    @FXML lateinit var editorialModeButtonImage: ImageView
    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController<String>
    @FXML lateinit var selected: Label

    private lateinit var languageName: String

    @FXML
    fun initialize() {
        editorialModeButtonImage.imageProperty()
            .bind(Bindings.`when`(isEditing).then(viewingImage).otherwise(editingImage))
        editorialModeButton.setOnMouseClicked { _ ->
            isEditing.set(isEditing.not().get())
        }
    }

    fun initSource(languageDir: File) {
        this.languageName = languageDir.name

        wordListViewController.run {
            initSource(simpleEngine(Language.fromDir(languageDir).words().keys, { it }))

            listview.prefHeightProperty().bind(Bindings.size(listview.items).multiply(LIST_CELL_SIZE).add(1 + LIST_PADDING * 2))
            listPadding(LIST_PADDING)
            listview.fixedCellSize = LIST_CELL_SIZE
        }
    }

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = languageName
            height = STAGE_HEIGHT
            width = STAGE_WIDTH
            isResizable = true
        }
    }

    companion object : Loadable<LanguageInspectionController>("languageInspection") {
        const val LIST_PADDING = 10.0
        const val STAGE_HEIGHT = 500.0
        const val STAGE_WIDTH = 600.0
        const val LIST_CELL_SIZE = 25.0

        private val isEditing = SimpleBooleanProperty(false)
        private val viewingImage = Image(resolveResourcePath("../image/eye.png"))
        private val editingImage = Image(resolveResourcePath("../image/pencil.png"))
    }
}