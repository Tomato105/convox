package org.shinytomato.convox.i

import javafx.scene.Scene
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxApplication

abstract class FXMLController {
    internal var stage: Stage
        set(value) { ConvoxApplication.stage = value }
        get() = ConvoxApplication.stage
    internal var scene: Scene
        set(value) { ConvoxApplication.stage.scene = value }
        get() = ConvoxApplication.stage.scene
    abstract fun whenLoad()
//    fun setEnv(stage: Stage, scene: Scene) {
//        this.stage = stage
//        this.scene = scene
//    }
}