package org.shinytomato.convox.fxml

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
import org.shinytomato.convox.i.FXMLController
import org.shinytomato.convox.i.IGetSelected
import org.shinytomato.convox.i.Loadable

class MainController : FXMLController(), IGetSelected<TextFlow> {

    @FXML lateinit var selected: Label
    @FXML lateinit var new: Button
    @FXML lateinit var open: Button
    @FXML lateinit var languageListView: Parent
    @FXML lateinit var languageListViewController: LanguageListController

    @FXML
    private fun initialize() {
        languageListViewController.getSelected = this
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
                languageListViewController.openCurrentlySelected()
        }
    }

    override fun whenSelected(selected: TextFlow, clickEvent: MouseEvent) {
        when (clickEvent.clickCount) {
            1 -> {
                this.selected.text = selected.children.joinToString(separator = "") { (it as Text).text }
                open.isDisable = false
            }

            2 -> languageListViewController.openCurrentlySelected()
        }
    }

    fun newButton() {
        ConvoxAction.mainPage(stage = stage)
    }

    fun openButton(): Unit = languageListViewController.openCurrentlySelected()

    companion object : Loadable("main/main")
}