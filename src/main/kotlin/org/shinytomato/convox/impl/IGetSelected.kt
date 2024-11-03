package org.shinytomato.convox.impl

import javafx.scene.input.MouseEvent

interface IGetSelected<T> {
    fun whenSelected(selected: T, clickEvent: MouseEvent)
    companion object {
        fun <T> IGetSelected<T>.setSelector(selector: ISelector<T>) {
            selector.getSelected = this
        }
    }
}

interface ISelector<T> {
    var getSelected: IGetSelected<T>?
    fun whenSelected(event: MouseEvent)
}