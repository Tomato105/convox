package org.shinytomato.convox.pages.languageInspection

import javafx.beans.property.ObjectProperty
import org.shinytomato.convox.data.word.Word

interface WordInspectionPage {
    val selectedWord: ObjectProperty<Word?>
    fun initWordProperty(wordProperty: ObjectProperty<Word?>) = selectedWord.bind(wordProperty)
}