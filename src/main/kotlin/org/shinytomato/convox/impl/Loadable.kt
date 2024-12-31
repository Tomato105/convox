package org.shinytomato.convox.impl

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.resolveResource

abstract class Loadable<C: FXMLController>() {

    abstract val configuration: PageConfiguration

    inline fun loadScene(
        stage: Stage,
        preAction: C.(Scene) -> Unit = {_: Scene -> },
    ): C {
        val (parent, controller) = loadFxml<Parent, C>(configuration.fxmlLocation)
        val scene = Scene(parent)

        // 제공하는 scene은 아직 stage에 적용하지 않은 scene 이므로 필요함 stage.scene으로 얻을 수 없음.
        return controller.apply {
            // Stage 객체 전달
            this.stage = stage
            configuration.applyTo(stage)
            preAction(scene)
            whenLoad(scene)
            stage.scene = scene
        }
    }

    companion object {
        fun <P: Parent, C> loadFxml(
            fxmlLocation: String
        ): Pair<P, C> {
            val loader = FXMLLoader(resolveResource("pages/$fxmlLocation"))
            val fxml = loader.load<P>()
            return fxml to loader.getController<C>()
        }

        fun <P: Parent, C> loadFxml(
            fxmlLocation: String,
            preAction: C.(P) -> Unit
        ): Pair<P, C> {
            val pair = loadFxml<P, C>(fxmlLocation)
            pair.second.preAction(pair.first)
            return pair
        }
    }
}
