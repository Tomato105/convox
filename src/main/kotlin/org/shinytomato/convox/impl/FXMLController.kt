package org.shinytomato.convox.impl

import javafx.scene.Scene
import javafx.stage.Stage


// stage를 ConvoxAction에서 관리하면 복수 창 관리가 어려움
open class FXMLController {
    lateinit var stage: Stage
    open fun whenLoad(scene: Scene) {}
}