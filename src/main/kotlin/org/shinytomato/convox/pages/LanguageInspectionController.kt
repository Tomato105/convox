package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import org.shinytomato.convox.data.ResourceManager.resolveResourcePath
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.data.word.Word
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected
import org.shinytomato.convox.impl.IGetSelected.Companion.setSelector
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.searchableList.ListViewEngine.Companion.simpleEngine
import org.shinytomato.convox.impl.searchableList.SearchableListController
import java.io.File

class LanguageInspectionController : FXMLController(), IGetSelected<Word> {

    @FXML lateinit var editorialModeButton: Button
    @FXML lateinit var editorialModeButtonImage: ImageView
    @FXML lateinit var wordListView: Parent
    @FXML lateinit var wordListViewController: SearchableListController<Word>
    @FXML lateinit var wordName: Label

    private lateinit var languageName: String
    private val selectedItem: SimpleObjectProperty<Word?> = SimpleObjectProperty(null)

    @FXML
    fun initialize() {
        wordName.textProperty().bind(Bindings.createStringBinding({
            selectedItem.get()?.name ?: "단어를 선택하십시오"
        }, selectedItem))

        setSelector(wordListViewController)
        editorialModeButtonImage.imageProperty()
            .bind(Bindings.`when`(isEditing).then(viewingImage).otherwise(editingImage))
        editorialModeButton.setOnMouseClicked { _ ->
            isEditing.set(isEditing.not().get())
        }
    }

    fun initSource(languageDir: File) {
        this.languageName = languageDir.name

        wordListViewController.run {
            initEngine(simpleEngine(Language.fromDir(languageDir).words.values.flatten(), ::displayWord))

            listview.prefHeightProperty().bind(Bindings.size(listview.items).multiply(LIST_CELL_SIZE).add(1 + LIST_PADDING * 2))
            listPadding(LIST_PADDING)
            listview.fixedCellSize = LIST_CELL_SIZE
        }
    }

    private fun displayWord(word: Word): String = word.name

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = languageName
            height = STAGE_HEIGHT
            width = STAGE_WIDTH
            isResizable = true
        }
    }

    override fun whenSelected(
        selected: Word,
        clickEvent: MouseEvent,
    ) = selectedItem.set(selected)

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