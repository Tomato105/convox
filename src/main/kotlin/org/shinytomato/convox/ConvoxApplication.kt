package org.shinytomato.convox

import javafx.application.Application
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.fxml.MainController
import java.net.URL

class ConvoxApplication : Application() {

    init {
        instance = this
    }

    /*internal lateinit var stage: Stage
    internal var scene: Scene
        set(value) { stage.scene = value }
        get() = stage.scene*/

    override fun start(stage: Stage) {
        ConvoxApplication.stage = stage
//        LanguageListFController.loadFXML(stage)
        mainPage(stage)
        stage.show()
    }

    companion object {
        lateinit var instance: ConvoxApplication
        lateinit var stage: Stage
        fun String.getResource(): URL = (instance.javaClass.getResource(this) ?: null.also { println("shinyerror: cannnot find: $this") })!!
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) { MainController.loadFXML(stage) }
    fun languageStructure(stage: Stage, selected: String) {
        stage.hide()
        TODO()
    }
}

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
