package org.shinytomato.convox.controllers.main

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
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
import org.shinytomato.convox.ConvoxApplication.ApplicationState.stage
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.IGetSelected
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.SearchableListController

class MainController : FXMLController(), IGetSelected<TextFlow> {

    @FXML lateinit var selected: Label
    @FXML lateinit var new: Button
    @FXML lateinit var open: Button
    @FXML lateinit var languageListView: Parent
    @FXML lateinit var languageListViewController: SearchableListController

    var selectedItem: SimpleStringProperty = SimpleStringProperty()

    @FXML
    private fun initialize() {
        selected.textProperty().bind(Bindings.`when`(selectedItem.isEmpty)
            .then("언어를 선택하여 주십시오")
            .otherwise(selectedItem))

        languageListViewController.initInput(DataManager.loadLanguageList())
        languageListViewController.list.run {
            prefHeightProperty().bind(Bindings.size(items).multiply(38).add(1))
            fixedCellSize = 38.0
            maxHeight = 300.0
            prefWidth = 220.0
        }

        languageListViewController.getSelected = this
    }

    private fun openSelected() {
        ConvoxAction.languageInspection(if (selectedItem.isEmpty.get()) return else selectedItem.get())
    }

    override fun whenLoad(stage: Stage, scene: Scene) {
        stage.run {
            title = "Convox에 오신 것을 환영합니다"
            icons.add(Image("org/shinytomato/convox/image/icon.png"))
            width = 500.0
            height = 400.0
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
                open.isDisable = false
            }

            2 -> openSelected()
        }
    }

    fun newButton() {
        ConvoxAction.mainPage(stage = stage)
    }

    fun openButton(): Unit = openSelected()

    companion object : Loadable<MainController>("main/main")
}