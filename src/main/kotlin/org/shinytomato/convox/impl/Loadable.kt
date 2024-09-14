package org.shinytomato.convox.impl

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxApplication.ApplicationState.getResource

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

open class Loadable<T: FXMLController>(private val fxml: String) {
    fun loadFXML(
        stage: Stage,
        preAction: T.(Stage, Scene) -> Unit = { _: Stage, _: Scene -> },
    ): T {
        val loader = FXMLLoader("fxml/$fxml/$fxml.fxml".getResource())
        val scene = Scene(loader.load())

        // 제공하는 scene은 stage.scene 이 아니라 로드 중인 scene 이므로 필요함.
        return loader.getController<T>().apply {
            preAction(stage, scene)
            whenLoad(stage, scene)
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
