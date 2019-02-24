package net.mengb.warpin

import org.w3c.dom.Node
import kotlin.browser.document

internal class Updater(store: Data, private val elementSymbol: dynamic,
                       private val eventProxy: dynamic, private val root: org.w3c.dom.Element,
                       private val renderNode: dynamic) {
    val update = fun (nodes: Array<Child>) {
        nodes.forEach(this::diff)
    }

    fun diff (child: Child) {
        val block = child.renderChild!!
        child.children = emptyArray()
        child.block()
        child.children.forEachIndexed { i, it ->
            val old = child.rendered[i]
            when (it) {
                is String -> {
                    if (js("'str' in old") as Boolean) {
                        if (old.str != it) {
                            old.str = it
                            old.elm.data = it
                        }
                    } else {
                        old.elm.parentNode.replaceChild(document.createTextNode(it), old.elm)
                    }
                }
                is Element -> {
                    val oldElm = old[elementSymbol]
                    if (oldElm is Element && oldElm::class == it::class) {
                        js("for (var key in oldElm) it[key] = oldElm[key]")
                        val oldChild = old.nodes as Child
                        oldChild.renderChild = it.render()
                        diff(oldChild)
                    }
                }
                else -> {
                    if (js("'elm' in old") as Boolean) {
                        if (it == null && old.none == true) return
                        val dyn = it.asDynamic()
                        if (old != null &&
                                (old.none != true && (old.type == dyn.type as? String))
                                ) {
                            if (dyn.child == null) old.elm.textContent = ""
                            else {
                                val oldChild = old.nodes as Child
                                oldChild.renderChild = dyn.child as (Child.() -> Unit)
                                diff(oldChild)
                                setAttributions(root, old.elm, dyn.attr, old.attr, false, eventProxy)
                            }
                        } else {
                            val n = Child(null)
                            n.children = arrayOf(it)
                            renderNode(js("""{
                                appendChild: function (e) {
                                    old.elm.parentNode.replaceChild(e, old.elm)
                                }
                            }"""), n, old.id)
                        }
                    }
                }
            }
            null
        }
    }
}
