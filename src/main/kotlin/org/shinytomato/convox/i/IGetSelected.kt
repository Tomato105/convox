package org.shinytomato.convox.i

import javafx.scene.input.MouseEvent

interface IGetSelected<T> {
    fun whenSelected(selected: T, clickEvent: MouseEvent)
}