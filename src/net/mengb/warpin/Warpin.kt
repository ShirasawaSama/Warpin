package net.mengb.warpin

import kotlinext.js.jsObject
import org.w3c.dom.Node
import kotlin.browser.document

fun <T> render(
        data: (T.() -> Unit)? = null,
        root: String,
        errorHandler: ((e: Error) -> Unit)? = null,
        threshold: Int?,
        block: Child.() -> Unit
) = render(data, document.querySelector(root), errorHandler, threshold, block)

fun <T> render(
        data: (T.() -> Unit)? = null,
        root: org.w3c.dom.Element? = document.getElementById("app"),
        errorHandler: ((e: Error) -> Unit)? = null,
        threshold: Int? = 8,
        block: Child.() -> Unit
): Data {
    if (root == null) throw NullPointerException("Cannot find root element!")

    val renderList = js("new Set")
    val update: dynamic
    val store = Data(if (data == null) jsObject { } else jsObject(data), renderList)

    js("""
        var useFrame = 'requestAnimationFrame' in (window || global)
        var idle = false
        renderList.push = function (obj) {
            renderList.add(obj)
            if (!idle) {
                function exec () {
                    var set = new Set, tree = { }, willRender = []
                    renderList.forEach(function (i) { i.forEach(function (a) { set.add(a) }) })
                    renderList.clear()
                    idle = false
                    var result = Array.from(set)
                    if (result.length === 1) {
                        var a = result[0]
                        if (!a.sid) a.sid = a.id.split(',')
                    } else {
                        result = result.sort(function (a, b) {
                            return (a.sid || (a.sid = a.id.split(','))).length -
                                (b.sid || (b.sid = b.id.split(','))).length
                        })
                    }
                    result.forEach(function (child) {
                        var i = 0, sid = child.sid, l = sid.length - 1, node = tree
                        while (i < l) {
                            var id = sid[i], cache = node[id]
                            if (cache) {
                                if (cache.h) return
                                node = cache
                            } else node = node[id] = { c: child, p: node }
                            i++
                        }
                        if (!++node.n) node.n = 1
                        else if (node.n > threshold) node.h = true
                        if (!node.h) node[sid[i]] = { h: true, c: child, p: node }
                    })
                    ;(function h (node) {
                        if (node.h) willRender.push(node.c)
                        else for (var k in node) if (k !== 'p' && k !== 'c') h(node[k])
                    })(tree)
                    ;(function clear (data) {
                        for (var i in data.__cache) {
                            clear(data.__cache[i])
                        }
                        data.__elements.forEach(function (e) {
                            var sid = (e.sid || (e.sid = e.id.split(','))),
                                len = sid.length, node = tree, i = 0
                            while (node = node[sid[i++]]) if (node.h) {
                                data.__elements.delete(e)
                                return
                            }
                        })
                    })(store)
                    update(willRender)
                }
                if (useFrame) requestAnimationFrame(exec)
                else setTimeout(exec, 16)
                idle = true
            }
        }
    """)

    val handler = errorHandler ?: console.asDynamic().error

    val elementSymbol = js("Symbol('Element')")
    val eventProxy = js("""function (e) {
        e = e || window.event
        var target = event.target, nodes = []
        if (!target) target = event.target = event.srcElement
        do {
            var elm = target[elementSymbol]
            if (elm && elm.attr) {
                var type = 'on' + e.type.toLowerCase(), fn = elm.attr[type]
                if (fn) try { fn(e) } catch(a) { handler(a) }
                fn = elm.attr[type + 'capture']
                if (fn) nodes.push(fn)
            }
            target = target.parentNode
        } while (target !== root && target)
        var i = nodes.length
        while (i--) try { nodes[i]() } catch(a) { handler(a) }
    }""")

    var renderNode: dynamic = null
    renderNode = fun (node: dynamic, nodes: Child, id: String): Array<Any> {
        nodes.id = id
        val rendered = nodes.children.mapIndexed { index, it ->
            val i = "$id,$index"
            when (it) {
                is String -> {
                    val dom = document.createTextNode(it)
                    node.appendChild(dom)
                    js("{ str: it, elm: dom, id: i }")
                }
                null -> {
                    val dom = document.createComment("")
                    node.appendChild(dom)
                    js("{ elm: dom, id: i, none: true }")
                }
                is Element -> {
                    if (it.storePrivate == null) {
                        it.storePrivate = store
                        val dyn: dynamic = it
                        if (js("typeof dyn.init === 'function'") as Boolean) dyn.init(store)
                    }
                    val dom = document.createDocumentFragment()
                    val renderFn = it.render()
                    val childNode = Child(renderFn)
                    childNode.renderFn()
                    val ret = renderNode(dom, childNode, i)
                    node.appendChild(dom)
                    it.treeNodes = ret
                    it.element = dom
                    ret[elementSymbol] = it
                    ret.nodes = childNode
                    ret
                }
                is Array<*> -> {
                    val dom = document.createDocumentFragment()
                    renderNode(dom, it.asDynamic().nodes as Child, i)
                    node.appendChild(dom)
                    js("{ elm: dom }")
                }
                else -> {
                    val dyn: dynamic = it
                    val dom = document.createElement(dyn.type as? String ?: "div")
                    val attr = dyn.attr
                    if (attr != null) setAttributions(root, dom, attr, null, false, eventProxy)
                    dyn.elm = dom
                    node.appendChild(dom)
                    if (dyn.child != null) {
                        val renderFn = dyn.child as (Child.() -> Unit)
                        val childNode = Child(renderFn)
                        childNode.renderFn()
                        renderNode(dom, childNode, i)
                        dyn.nodes = childNode
                    }
                    dom.asDynamic()[elementSymbol] = it
                    dyn
                }
            } as Any
        }.toTypedArray()
        nodes.children = emptyArray()
        nodes.rendered = rendered
        return rendered
    }
    update = Updater(store, elementSymbol, eventProxy, root, renderNode).update

    val nodes = Child(block)
    nodes.block()
    renderNode(root, nodes, "0")
    return store
}
