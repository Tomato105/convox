package org.shinytomato.convox

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlin.math.sqrt

class MainApp : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load())

        stage.run {
            title = "Convox에 오신 걸 환영합니다"
            setScene(scene)
            icons.add(Image(javaClass.getResourceAsStream("image/icon.png")))
            width = 400.0 * sqrt(1.4141356)
            height = 400.0
            show()
        }
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}
