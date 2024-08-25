package org.shinytomato.convox.impl

import javafx.scene.input.MouseEvent

interface IGetSelected<T> {
    fun whenSelected(selected: T, clickEvent: MouseEvent)
}