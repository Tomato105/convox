package org.shinytomato.convox.i

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxApplication.Companion.getResource
/*
interface ILoadable {
    val fxml: String

    fun loadFXML(stage: Stage, action: FXMLController.() -> Unit = {}): FXMLController {
        val loader = FXMLLoader("fxml/$fxml.fxml".getResource())
        val scene = Scene(loader.load())

        return loader.getController<FXMLController>().apply {
            this.stage = stage
            this.scene = scene
            whenLoad()
            action()
            stage.scene = scene
        }
    }
}*/

open class Loadable(private val fxml: String) {
    fun loadFXML(stage: Stage, action: FXMLController.() -> Unit = {}): FXMLController {
        val loader = FXMLLoader("fxml/$fxml.fxml".getResource())
        val scene = Scene(loader.load())

        return loader.getController<FXMLController>().apply {
            whenLoad()
            action()
            stage.scene = scene
        }
    }
}
/*

abstract class Loadable(val string: String) {
    abstract val fxml: String
    fun loadFXML(stage: Stage, action: FXMLController.() -> Unit = {}): FXMLController {
        val loader = FXMLLoader("fxml/$fxml.fxml".getResource())
        val scene = Scene(loader.load())

        return loader.getController<FXMLController>().apply {
            this.stage = stage
            this.scene = scene
            whenLoad()
            action()
            stage.scene = scene
        }
    }
}*/
