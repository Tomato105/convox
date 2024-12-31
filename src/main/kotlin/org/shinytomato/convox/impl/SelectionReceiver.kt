package org.shinytomato.convox.impl

interface SelectionReceiver<T> {
    fun receiveSelection(selected: T)
}

interface SelectionGiver<T> {
    fun giveSelection(selected: T): Unit?
}