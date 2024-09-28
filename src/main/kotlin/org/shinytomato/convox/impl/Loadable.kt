package org.shinytomato.convox.impl

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.data.ResourceManager.resolveResource

open class Loadable<T: FXMLController>(private val fxml: String) {
    fun loadFXML(
        stage: Stage,
        preAction: T.(Stage, Scene) -> Unit = { _: Stage, _: Scene -> },
    ): T {
        val loader = FXMLLoader(resolveResource("../pages/$fxml/$fxml.fxml"))
        val scene = Scene(loader.load())

        // 제공하는 scene은 아직 stage에 적용하지 않은 scene 이므로 필요함 stage.scene으로 얻을 수 없음.
        return loader.getController<T>().apply {
            this.stage = stage
            preAction(stage, scene)
            whenLoad(stage, scene)
            stage.scene = scene
        }
    }
}
