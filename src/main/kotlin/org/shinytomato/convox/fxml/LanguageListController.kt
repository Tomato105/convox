package org.shinytomato.convox.fxml

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.shinytomato.convox.ConvoxAction
import org.shinytomato.convox.data.DataManager
import org.shinytomato.convox.i.FXMLController
import org.shinytomato.convox.i.IGetSelected
import org.shinytomato.convox.i.Loadable
import java.util.function.Predicate

class LanguageListController : FXMLController() {

    @FXML
    lateinit var search: TextField
    private var languages = FilteredList(FXCollections.observableList(DataManager.loadLanguageList()))

    @FXML
    private lateinit var languageList: ListView<TextFlow>

    internal var getSelected: IGetSelected? = null


    @FXML
    private fun initialize() {

        /*val bolding: (string: String, another: String) -> TextFlow = { it, another->
            if (another.isEmpty()) TextFlow(Text(it))
            else {
                val index = it.indexOf(another)
                TextFlow(
                    Text(it.substring(0..<index.also(::println))),
                    Text(it.substring(index..<index + another.length)).apply { style = "-fx-font-weight: bold" },
                    Text(it.substring(index + another.length..<it.length)),
                )
            }
        }*/

        fun updateList(text: String) {
            if (text.isEmpty()) {
                languages.predicate = Predicate { true }
                languageList.itemsProperty().bind(
                    SimpleObjectProperty(
                        FXCollections.observableList(languages.map { TextFlow(Text(it)) })
                    )
                )
            } else {
                languages.predicate = Predicate { it.contains(text) }
                languageList.itemsProperty().bind(
                    SimpleObjectProperty(
                        FXCollections.observableList(languages.map {
                            val index = it.indexOf(text)
                            val point = index+text.length
                            TextFlow(
                                Text(it.substring(0..<index)),
                                Text(it.substring(index..<point))
                                    .apply { style = "-fx-font-weight: bold" },
                                Text(it.substring(point..<it.length)),
                            )
                        })
                    )
                )
            }
        }

        languageList.run {
//            languageList.itemsProperty().bind(SimpleObjectProperty(FXCollections.observableList(languages.map { TextFlow(Text(it)) })))
            updateList("")
            //inp를 레퍼런스로 전달 못해주나...

            prefHeightProperty().bind(Bindings.size(languageList.items).multiply(38).add(1))

            search.textProperty().addListener { _, _, text ->
                updateList(text)
            }
        }
    }

    fun whenSelected(event: MouseEvent) {
        getSelected?.whenSelected(
            languageList.selectionModel.selectedItem.children.joinToString(separator = "") { (it as Text).text },
            event.clickCount
        )
    }

    fun openCurrentlySelected() {
        val selected = languageList.selectionModel.selectedItem.also(::println) ?: return
        ConvoxAction.languageStructure(selected.children.joinToString(separator = "")  { (it as Text).text })
    }

    companion object : Loadable("languageList")
}