package net.mengb.warpin

import kotlinext.js.jsObject
import org.w3c.dom.DocumentFragment

abstract class Element(block: (Child.() -> Unit)? = null) {
    internal var storePrivate: Data? = null
    var slot: Child? = null
    internal var element: DocumentFragment? = null
    internal var treeNodes: Array<Any>? = null
    private var stateList: Array<Data>? = null
//    internal var beforeRender = ArrayList<Element>()

    val store: Data
    get() = storePrivate ?: throw NullPointerException("Cannot access member 'store' in constructor!")

    init {
        if (block != null) {
            val node = Child(block)
            node.block()
            slot = node
        }
    }

    abstract fun render(): Child.() -> Unit
    @Suppress("NOTHING_TO_INLINE")
    inline fun h(noinline block: Child.() -> Unit) = block

    fun <T> createState(data: T.() -> Unit): Data {
        if (stateList == null) {
            stateList = emptyArray()
            val old = asDynamic().init
            asDynamic().init = if (old == null) fun () {
                val list = storePrivate!!.renderList
                stateList!!.forEach { it.renderList = list }
                stateList = null
            } else fun () {
                val list = storePrivate!!.renderList
                stateList!!.forEach { it.renderList = list }
                stateList = null
                old(storePrivate)
            }
        }
        val d = Data(jsObject(data), null)
        stateList.asDynamic().push(d)
        return d
    }
}
