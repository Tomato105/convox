package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.ResourceManager
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.searchableList.SearchableListController
import org.shinytomato.convox.impl.searchableList.simpleEngine
import java.io.File

class MainController : FXMLController(), IGetSelected<File> {

    @FXML lateinit var selected: Label
    @FXML lateinit var newButton: Button
    @FXML lateinit var openButton: Button
    @FXML lateinit var languageListView: Parent
    @FXML lateinit var languageListViewController: SearchableListController<File>

    private val selectedItem: SimpleObjectProperty<File> = SimpleObjectProperty(null)

    @FXML
    private fun initialize() {
        selected.textProperty().bind(Bindings.`when`(selectedItem.isNull)
            .then("언어를 선택하여 주십시오")
            .otherwise(selectedItem.name))

        languageListViewController.run {
            initOrigin(simpleEngine(ResourceManager.languageDirList()) { it.name })

            val binding = Bindings.size(listview.items).multiply(LIST_CELL_SIZE).add(1 + LIST_PADDING * 2)
            listview.prefHeightProperty().bind(binding)
            listPadding(LIST_PADDING)
            listview.fixedCellSize = LIST_CELL_SIZE
        }

        languageListViewController.getSelected = this
    }

    private fun openSelected() {
        ConvoxAction.languageInspection(stage, if (selectedItem.isNull.get()) return else selectedItem.get())
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

    override fun whenSelected(selected: File, clickEvent: MouseEvent) {
        when (clickEvent.clickCount) {
            1 -> {
                selectedItem.set(selected)
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

        private const val LIST_PADDING = 10.0
        private const val LIST_CELL_SIZE = 40.0
    }
}