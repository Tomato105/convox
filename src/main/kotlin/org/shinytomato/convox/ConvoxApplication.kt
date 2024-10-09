package org.shinytomato.convox

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.pages.LanguageInspectionController
import org.shinytomato.convox.pages.MainController
import java.io.File

class ConvoxApplication : Application() {

    override fun start(stage: Stage) {
        mainPage(stage)
        stage.show()
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) {
        MainController.loadFXML(stage)
    }

    fun languageInspection(stage: Stage, selected: File) {
        stage.hide()
        println("selected: $selected")
        LanguageInspectionController.loadFXML(stage) { _: Stage, _: Scene ->
            initSource(selected)
        }
        stage.show()
    }
}

fun <K, V> Map<K, V>.toHashMap() = HashMap(this)

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
