package org.shinytomato.convox.pages

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.ResourceManager
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.SelectionReceiver
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.impl.PageConfiguration
import org.shinytomato.convox.pages.searchableList.SearchableListController
import java.io.File
import java.util.concurrent.Callable

class MainController : FXMLController(), SelectionReceiver<Pair<File, MouseEvent>> {

    @FXML lateinit var selectedLanguage: Label
    @FXML lateinit var openButton: Button
    @FXML lateinit var languageListView: Parent
    @FXML lateinit var languageListViewController: SearchableListController<File>

    private val selectedItem: SimpleObjectProperty<File> = SimpleObjectProperty(null)

    @FXML
    private fun initialize() {
        selectedLanguage.bindTextAbout(selectedItem) { selectedItem.get()?.name ?: "언어를 선택하여 주십시오" }

        languageListViewController.run {
            initSimpleEngine(ResourceManager.languageDirs(), File::getName)

            val binding = Bindings.size(listview.items).multiply(LIST_CELL_SIZE).add(1 + LIST_PADDING * 2)
            listview.prefHeightProperty().bind(binding)
            listPadding(LIST_PADDING)
            listview.fixedCellSize = LIST_CELL_SIZE
        }

        languageListViewController.selectionReceiver = this
    }

    fun openSelected() {
        ConvoxAction.languageInspection(stage, if (selectedItem.isNull.get()) return else selectedItem.get())
    }

    override fun whenLoad(scene: Scene) {

        scene.addEventFilter(KeyEvent.KEY_PRESSED) { key: KeyEvent ->
            if (key.code == KeyCode.ENTER)
                openSelected()
        }
    }

    override fun receiveSelection(pair: Pair<File, MouseEvent>) {
        val selected = pair.first
        val clickEvent = pair.second

        when (clickEvent.clickCount) {
            1 -> {
                selectedItem.set(selected)
                openButton.isDisable = false
            }

            2 -> openSelected()
        }
    }

    fun newButton() {}

    companion object : Loadable<MainController>() {

        override val configuration: PageConfiguration = PageConfiguration(
            fxmlLocation = "main/main.fxml",
            title = "Convox에 오신 것을 환영합니다",
            height = 400.0,
            width = 500.0,
            isResizable = false
        )

        private const val LIST_PADDING = 10.0
        private const val LIST_CELL_SIZE = 40.0
    }
}

inline fun <T> Labeled.bindTextAbout(trigger: ObjectProperty<T>, crossinline callable: (T) -> String) {
    textProperty().bind(Bindings.createStringBinding(Callable { callable(trigger.get()) }, trigger))
}

inline fun <T> ObjectProperty<T>.produceTextFor(label: Labeled, crossinline callable: (T?) -> String): Unit =
    label.bindTextAbout(this, callable)

fun <T> ObjectProperty<T>.produceTextFor(vararg pairs: Pair<Labeled, (T?) -> String>) {
    for ((labeled, callable) in pairs) produceTextFor(labeled, callable)
}