package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.ResourceManager
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class MainController : FXMLController(), IGetSelected<TextFlow> {

    @FXML lateinit var selected: Label
    @FXML lateinit var newButton: Button
    @FXML lateinit var openButton: Button
    @FXML lateinit var languageListView: Parent
    @FXML lateinit var languageListViewController: SearchableListController

    private val selectedItem: SimpleStringProperty = SimpleStringProperty()

    @FXML
    private fun initialize() {
        selected.textProperty().bind(Bindings.`when`(selectedItem.isEmpty)
            .then("언어를 선택하여 주십시오")
            .otherwise(selectedItem))

        languageListViewController.run {
            initInput(ResourceManager.loadLanguageSet().toList())

            val binding = Bindings.size(list.items).multiply(LIST_CELL_HEIGHT).add(1 + LIST_PADDING * 2)
            list.prefHeightProperty().bind(binding)
            list.padding = Insets(LIST_PADDING, LIST_PADDING, LIST_PADDING, LIST_PADDING)
            list.fixedCellSize = LIST_CELL_HEIGHT
            list.prefWidth = LIST_WIDTH
        }

        languageListViewController.getSelected = this
    }

    private fun openSelected() {
        ConvoxAction.languageInspection(stage, if (selectedItem.isEmpty.get()) return else selectedItem.get())
    }

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = "Convox에 오신 것을 환영합니다"
            icons.add(Image("org/shinytomato/convox/image/application-icon.png"))
            width = STAGE_WIDTH
            height = STAGE_HEIGHT
            isResizable = false
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED) { key: KeyEvent ->
            if (key.code == KeyCode.ENTER)
                openSelected()
        }
    }

    override fun whenSelected(selected: TextFlow, clickEvent: MouseEvent) {
        when (clickEvent.clickCount) {
            1 -> {
                selectedItem.set(selected.children.joinToString(separator = "") { (it as Text).text })
                openButton.isDisable = false
            }

            2 -> openSelected()
        }
    }

    fun newButton() {
        ConvoxAction.mainPage(stage = stage)
    }

    fun openButton(): Unit = openSelected()

    companion object : Loadable<MainController>("main") {
        const val STAGE_HEIGHT = 400.0
        const val STAGE_WIDTH = 500.0
        const val LIST_WIDTH = 220.0

        private const val LIST_PADDING = 10.0
        private const val LIST_CELL_HEIGHT = 40.0
    }
}