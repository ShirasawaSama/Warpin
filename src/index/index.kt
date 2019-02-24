package index

import net.mengb.warpin.Element
import net.mengb.warpin.render
import kotlin.js.Math
import kotlin.math.PI

class TestA: Element() {
    private val state = createState<A> { test = false }

    override fun render() = h {
        button(attr = { onClick = { state["test"] = true } }) {
            +"Show PI!"
        }
        div({ style = "color:blue" }) {
            arrayOf("I", "think", "the", "PI", "is:")
                    .map { "$it " }
                    .forEach { +it }

            state["test"].pick {
                !PI
            }
        }
        div({ style = "color:green" }) {
            +"Also random: ${Math.random()}"
        }
    }
}

class Test: Element() {
    override fun render() = h {
        div({ style = "color:red" }) {
            val str = store["text"].string()

            button({ onClick = { store["text"] = "Don't touch me!" } }) { +str }
            div { +str }
            div { +TestA() }
        }
    }
}

interface A {
    var text: String
    var test: Boolean
}
fun main(args: Array<String>) {
    render<A>({ text = "Click me!" }) {
        +Test()
    }
}
