package org.shinytomato.convox.pages.languageInspection

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.shinytomato.convox.data.word.Word

class WordEditingController : WordInspectionPage {

    override val selectedWord: ObjectProperty<Word?> = SimpleObjectProperty<Word?>(null)
}