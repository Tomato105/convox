package org.shinytomato.convox

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.ConvoxApplication.ApplicationState.stage
import org.shinytomato.convox.pages.LanguageInspectionController
import org.shinytomato.convox.pages.MainController
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

        fun warnToString(code: String, description: String): String = "Warning (e-$code): $description"
        fun warn(code: String, description: String) {
            println(">>> ${warnToString(code, description)} <<<")
        }
        fun <T> T.warn(code: String, description: String): T {
            warn(code, description)
            return this
        }

        /*fun <T> T.warnIfNull(code: String, description: String): T {
            return this ?: warn(code, description)
        }

        fun <T> T.warnIf(code: String, description: String, action: T.() -> Boolean): T {
            return if (action()) warn(code, description) else this
        }*/

        fun String.getResource(): URL =
            (instance.javaClass.getResource(this).also { println("$this -> $it") } ?: null.also { warn("CA09","cannnot find: $this") })!!
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) {
        MainController.loadFXML(stage)
    }

    fun languageInspection(selected: String) {
        stage.hide()
        println("selected: $selected")
        LanguageInspectionController.loadFXML(stage) { _: Stage, _: Scene ->
            initInput(selected)
        }
        stage.show()
    }
}

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
