package org.shinytomato.convox

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import org.shinytomato.convox.ConvoxAction.mainPage
import org.shinytomato.convox.impl.FXMLController
import org.shinytomato.convox.impl.Loadable
import org.shinytomato.convox.pages.languageInspection.LanguageInspectionController
import org.shinytomato.convox.pages.MainController
import java.io.File
import java.net.URL

class ConvoxApplication : Application() {
    override fun start(stage: Stage) {
        mainPage(stage)
        stage.icons.add(Image("org/shinytomato/convox/image/application-icon.png"))
        stage.show()
    }
}

object ConvoxAction {
    fun mainPage(stage: Stage) {
        loadPage(stage, MainController)
    }

    fun languageInspection(stage: Stage, selected: File) {
        hideAndLoad(stage, LanguageInspectionController) { _ -> initSource(selected) }
    }

    inline fun <T : FXMLController> loadPage(
        stage: Stage,
        loadable: Loadable<T>,
        preAction: T.(Scene) -> Unit = { _: Scene -> },
    ): T = loadable.loadScene(stage, preAction)

    inline fun <T : FXMLController> hideAndLoad(
        stage: Stage,
        loadable: Loadable<T>,
        preAction: T.(Scene) -> Unit = { _: Scene -> },
    ): T {
        stage.hide()
        return loadPage(stage, loadable, preAction).also { stage.show() }
    }

    fun Any.resolveResource(string: String): URL? = this@ConvoxAction.javaClass.getResource(string).also(::println)
    fun Any.resolveResourcePath(string: String): String? = this@ConvoxAction.resolveResource(string)?.toString()
}

fun <K, V> Map<K, V>.toHashMap() = HashMap(this)
inline fun <T> T.showState(howToShow: (T) -> String? = { "$it" }): T = also {
    val str = howToShow(it)
    if (str != null) println(it)
}

fun main() {
    Application.launch(ConvoxApplication::class.java)
}
