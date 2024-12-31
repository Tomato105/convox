package org.shinytomato.convox.pages.searchableList

// 어차피 String은 ref로 넘겨지니까 별 손해 X
interface Displayable<T> {
    val display: String
    val item: T
}