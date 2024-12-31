package org.shinytomato.convox.pages.languageInspection

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.ConvoxAction.resolveResourcePath
import org.shinytomato.convox.data.Language
import org.shinytomato.convox.data.word.Word
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.SelectionReceiver
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.Loadable.Companion.loadFxml
import org.shinytomato.convox.impl.PageConfiguration
import org.shinytomato.convox.pages.searchableList.SearchableListController
import java.io.File

class LanguageInspectionController : FXMLController(), SelectionReceiver<Pair<Word, MouseEvent>>{

    @FXML lateinit var wordInspection: AnchorPane
    @FXML lateinit var editorialModeButton: Button
    @FXML lateinit var editorialModeButtonImage: ImageView
    @FXML lateinit var wordListViewController: SearchableListController<Word>

    val selectedWord: SimpleObjectProperty<Word?> = SimpleObjectProperty(null)

    private lateinit var language: Language

    val description =
        loadFxml<Parent, WordDescriptionController>("languageInspection/wordDescription.fxml").run {
            second.initWordProperty(selectedWord)
            first
        }
    val editing =
        loadFxml<Parent, WordEditingController>("languageInspection/wordEditing.fxml").run {
            second.initWordProperty(selectedWord)
            first
        }

    @FXML
    fun initialize() {

        // TODO 이게 왜 이렇게 되나 그냥 isEditing에 맞게 뭐 어떻게 안 되나
        showDescription()
        isEditing.addListener { _, _, isEditing: Boolean ->
            if (isEditing) showEditing()
            else showDescription()
        }

        // receive selected words
        wordListViewController.selectionReceiver = this

        // bindings
        editorialModeButtonImage.imageProperty()
            .bind(Bindings.`when`(isEditing).then(viewingImage).otherwise(editingImage))
    }

    fun editorialModeButtonClicked() {
        isEditing.set(isEditing.not().get())
    }

    private fun showDescription() {
        wordInspection.children.setAll(description)
    }

    private fun showEditing() {
        wordInspection.children.setAll(editing)
    }

    fun initSource(languageDir: File) {
        language = Language.Companion.fromDir(languageDir)

        wordListViewController.run {
            initSimpleEngine(language.words.values.flatten()) { word -> word.name }

            listview.prefHeightProperty().bind(Bindings.size(listview.items).multiply(LIST_CELL_SIZE).add(1 + LIST_PADDING * 2))
            listPadding(LIST_PADDING)
            listview.fixedCellSize = LIST_CELL_SIZE
        }
    }

    override fun whenLoad(scene: Scene) {
        stage.title = "사전 보기: ${language.name}"
        scene.addEventFilter(KeyEvent.KEY_PRESSED) { key: KeyEvent ->
            if (key.code == KeyCode.ESCAPE)
                returnToMain()
        }
    }

    fun returnToMain(): Unit = ConvoxAction.mainPage(stage)


    override fun receiveSelection(selected: Pair<Word, MouseEvent>): Unit =
        selectedWord.set(selected.first)

    companion object : Loadable<LanguageInspectionController>() {

        override val configuration: PageConfiguration = PageConfiguration(
            fxmlLocation = "languageInspection/languageInspection.fxml",
            title = "사전 보기",
            height = 500.0,
            width = 600.0,
            isResizable = true
        )

        const val LIST_PADDING = 10.0
        const val LIST_CELL_SIZE = 25.0

        private val isEditing = SimpleBooleanProperty(false)
        private val viewingImage = Image(resolveResourcePath("image/eye.png"))
        private val editingImage = Image(resolveResourcePath("image/pencil.png"))
    }
}