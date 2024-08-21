package org.shinytomato.convox

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.ConvoxApplication.ApplicationState.stage
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

    companion object ApplicationState {
        internal lateinit var instance: ConvoxApplication
        internal lateinit var stage: Stage
        var scene: Scene
            get() = stage.scene
            set(value) { stage.scene = value }

        fun String.getResource(): URL = (instance.javaClass.getResource(this) ?: null.also { println("shinyerror: cannnot find: $this") })!!
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) {
        MainController.loadFXML(stage)
    }
    fun languageStructure(selected: String) {
        stage.hide()
        println("selected: $selected")
    }
}

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
