package org.shinytomato.convox.pages.languageInspection

import org.shinytomato.convox.data.word.Word
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.Label
import org.shinytomato.convox.pages.produceTextFor

class WordDescriptionController : WordInspectionPage {
    @FXML lateinit var wordName: Label
    @FXML lateinit var wordPronunciation: Label

    override val selectedWord: ObjectProperty<Word?> = SimpleObjectProperty<Word?>(null)

    override fun initWordProperty(wordProperty: ObjectProperty<Word?>) {
        selectedWord.bind(wordProperty)
        selectedWord.produceTextFor(
            wordPronunciation to { "[${it?.pronunciation ?: '-'}]" },
            wordName to { it?.name ?: "단어를 입력하세요." }
        )
    }
}