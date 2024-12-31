package org.shinytomato.convox.data.word


sealed class WordRef {

    fun get() = inner!!
    fun idHexDec() = id.toString(16)
    abstract val id: Int
    abstract val inner: Word?
    abstract fun validateRef(wordMap: HashMap<Int, Word>): InstanceRef?

    class IdRef(override val id: Int) : WordRef() {
        override val inner: Word? = null
        override fun validateRef(wordMap: HashMap<Int, Word>): InstanceRef? {
            return InstanceRef(wordMap[id] ?: return null)
        }
        override fun toString() = "IdRef(id=$id)"
    }

    class InstanceRef(word: Word) : WordRef() {
        override val id: Int = word.id
        override val inner: Word = word
        override fun validateRef(wordMap: HashMap<Int, Word>): InstanceRef = this
        override fun toString() = "InstanceRef(id=$id)"
    }
}