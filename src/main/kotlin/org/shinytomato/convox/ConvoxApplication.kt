package org.shinytomato.convox

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.ConvoxApplication.ApplicationState.stage
import org.shinytomato.convox.controllers.languageInspection.LanguageInspectionController
import org.shinytomato.convox.controllers.main.MainController
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
            set(value) {
                stage.scene = value
            }

        // code: 패키지명 첫글자 + 아무문자나(클래스내에서 통일) + 발견된순서 + 아무숫자나
        fun unintendedBehavior(code: String, description: String): String = "Unintended Behavior (e-$code): $description"

        fun String.getResource(): URL =
            (instance.javaClass.getResource(this).also { println("$this -> $it") } ?: null.also { unintendedBehavior("CA09","cannnot find: $this") })!!
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) {
        MainController.loadFXML(stage)
    }

    fun languageInspection(selected: String) {
        stage.hide()
        println("selected: $selected")
        LanguageInspectionController.loadFXML(stage).initInput(selected)
        stage.show()
    }
}

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
