package org.shinytomato.convox.impl

import javafx.stage.Stage

// data 하지마 어차피 비교한다고 해도 같은 fxml 건지 아닌지가 중요해 값비교 X
class PageConfiguration(
    val fxmlLocation: String,
    private val title: String? = null,
    private val height: Double? = null,
    private val width: Double? = null,
    private val isResizable: Boolean? = null,
    private val minHeight: Double? = null,
    private val maxHeight: Double? = null,
    private val minWidth: Double? = null,
    private val maxWidth: Double? = null,
) {

    fun applyTo(stage: Stage) {
        if (title != null) stage.title = title
        if (height != null) stage.height = height
        if (width != null) stage.width = width
        if (isResizable != null) stage.isResizable = isResizable
        if (minHeight != null) stage.minHeight = minHeight
        if (maxHeight != null) stage.maxHeight = maxHeight
        if (minWidth != null) stage.minWidth = minWidth
        if (maxWidth != null) stage.maxWidth = maxWidth
    }
}