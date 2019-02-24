package net.mengb.warpin

import kotlinext.js.jsObject

@DslMarker
private annotation class WarpinDslMarker

interface HtmlNode {
    var child: (Child.() -> Unit)?
    var type: String?
    var elm: org.w3c.dom.Element
    var attr: dynamic
}
// pick: Boolean = false
@Suppress("NOTHING_TO_INLINE")
@WarpinDslMarker
class Child(var renderChild: (Child.() -> Unit)?, private val parent: Child? = null) {
    internal var rendered: Array<dynamic> = js("null") // Mustn't change this
    var id: String? = null // Mustn't change this
    var sid: Array<Number>? = null // Mustn't change this
    var children = emptyArray<Any?>()

    inline fun push(b: Any) {
        children.asDynamic().push(b)
    }

    inline fun none(num: Int = 1) {
        for (i in 1..num) children.asDynamic().push(null)
    }

    inline fun div(noinline block: Child.() -> Unit) {
        push(jsObject<HtmlNode> { child = block })
    }
    inline fun div(attr: Attributions.() -> Unit, noinline block: (Child.() -> Unit)? = null) {
        push(jsObject<HtmlNode> {
            child = block
            this.attr = jsObject(attr)
        })
    }
    inline fun div(name: String) {
        push(jsObject<HtmlNode> { attr = jsObject<Attributions> { className = name } })
    }

    inline fun button(noinline block: Child.() -> Unit) {
        push(jsObject<HtmlNode> {
            child = block
            type = "button"
        })
    }
    inline fun button(attr: Attributions.() -> Unit, noinline block: (Child.() -> Unit)? = null) {
        push(jsObject<HtmlNode> {
            child = block
            this.attr = jsObject(attr)
            type = "button"
        })
    }
    inline fun button(name: String) {
        push(jsObject<HtmlNode> { attr = jsObject<Attributions> {
            className = name
            type = "button"
        } })
    }

    inline operator fun Element.unaryPlus() {
        this@Child.push(this)
//        this@Child.elements?.add(this)
    }
    inline operator fun String.unaryPlus() {
        push(this)
    }
    inline operator fun Number.not() {
        push(this.toString())
    }
    inline operator fun Unit?.unaryPlus() { children.asDynamic().push(null) }

    operator fun Data.get(key: Any?): Data {
        __elements.add(parent ?: this@Child)
//        this@Child.observers.asDynamic().push(this)
        var value: Data? = __cache[key] as? Data
        if (value == null) {
            val v = data[key]
            if (v != null) {
                value = Data(v, renderList)
                __cache[key] = value
            }
        }
        return value ?: throw NullPointerException("Cannot get member: $key")
    }

    inline fun pick(condition: Boolean, count: Int? = 1, b1: Child.() -> Unit) {
        val num = count ?: 1
        if (condition) {
            var size = this.children.size
            this.b1()
            size = this.children.size - size
            if (size > num) throw Exception("Num is out of size.")
            if (size != num) none(num - size)
        } else none(num)
    }

    inline fun pick(condition: Boolean, count: Int? = 1, b1: Child.() -> Unit, b2: Child.() -> Unit) {
        val num = count ?: 1
        var size = this.children.size
        if (condition) {
            this.b1()
            size = this.children.size - size
            if (size > num) throw Exception("Num is out of size.")
        } else {
            this.b2()
            size = this.children.size - size
            if (size > num) throw Exception("Num is out of size.")
        }
        if (size != num) none(num - size)
    }

    inline fun Boolean.pick(count: Int? = 1, b1: Child.() -> Unit) {
        pick(this, count, b1)
    }

    inline fun Boolean.pick(count: Int? = 1, b1: Child.() -> Unit, b2: Child.() -> Unit) {
        pick(this, count, b1, b2)
    }

    inline fun Data.pick(count: Int? = 1, b1: Child.() -> Unit) {
        pick(this.boolean(), count, b1)
    }

    inline fun Data.pick(count: Int? = 1, b1: Child.() -> Unit, b2: Child.() -> Unit) {
        pick(this.boolean(), count, b1, b2)
    }

    inline fun <T> Array<out T>.forEach(action: Child.(T) -> Unit) {
        val child = Child(null, this@Child)
        for (element in this) child.action(element)
        child.children.asDynamic().nodes = child
        push(child.children)
    }
}
