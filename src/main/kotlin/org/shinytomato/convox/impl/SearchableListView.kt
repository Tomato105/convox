package org.shinytomato.convox.impl

import javafx.beans.value.ChangeListener
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import javafx.scene.text.TextFlow


/*
    TODO:
     여기에 제너릭 추가하기.
     구현할 것:
     displayMapper: 표시되는 글자를 정함. default는 toString()혹은 Displayable의 toDisplayed() or Displayable: 표시되는 글자가 있다는 interface
     검색 방식: 기본적으로 문자열 검색
     표시 방식: 기본적으로 선택된 글자 bold체
     검새되는 글자 방식: 기본적으로 toDisplayed()
     이거 이 중에 필요없는 건 추려라
 */

class SearchableListView(
    private val listview: ListView<TextFlow>,
    private val origin: List<String>,
    private val searchedBy: TextField,
) {

    private val listener = ChangeListener { _, _, new -> updateList(new) }

    init {
        updateList("")
        searchedBy.textProperty().addListener(listener)
    }

    fun close() {
        searchedBy.textProperty().removeListener(listener)
    }

    private fun updateList(text: String) {
        listview.items.setAll(
            if (text.isEmpty()) {
                origin.map { TextFlow(Text(it)) }
            } else {
                origin.mapNotNull {
                    val index = it.indexOf(text)
                    if (index == -1) return@mapNotNull null
                    val point = index + text.length
                    TextFlow(
                        Text(it.substring(0..<index)),
                        Text(it.substring(index..<point))
                            .apply { style = "-fx-font-weight: bold" },
                        Text(it.substring(point..<it.length)),
                    )
                }
            }
        )
    }

    /*constructor(listview: ListView<TextFlow>, list: List<String>, searchedBy: TextField) : this(
        listview,
        FilteredList(FXCollections.observableList(list)),
        searchedBy
    )*/
}