package net.mengb.warpin

private val IS_NON_DIMENSIONAL = js("/acit|ex(?:s|g|n|p|\\\${'\$'})|rph|ows|mnc|ntw|ine[ch]|zoo|^ord/i")
private val XLINK = js("/^xlink:?/")

internal fun setAttributions(root: org.w3c.dom.Element, node: dynamic, v: dynamic,
                                    old: dynamic, isSvg: Boolean, eventProxy: dynamic) {
    js("""
        for (var name in v) {
            var value = v[name]
            if (name === 'key') {}
            else if (name === 'class' && !isSvg) node.className = value || ''
            else if (name === 'innerHTML') node.innerHTML = value
            else if (name !== 'list' && name !== 'type' && !isSvg && name in node) {
                try { node[name] = value == null ? '' : value } catch (e) { }
                if ((value == null || value === false) && name != 'spellcheck') node.removeAttribute(name)
            } else {
                var type = typeof value
                if (name === 'style') {
                    var ot = typeof old === 'string'
                    if (!value || type === 'string' || ot) node.style.cssText = value || ''
                    if (value && type === 'object') {
                        if (!ot) for (var i in old) if (!(i in value)) node.style[i] = ''
                        for (var i in value) {
                            node.style[i] = typeof value[i] === 'number' &&
                                IS_NON_DIMENSIONAL.test(i) === false ? (value[i] + 'px') : value[i]
                        }
                    }
                } else if (name[0] === 'o' && name[1] === 'n') {
                    var f = v[name]
                    var name = name.toLowerCase()
                    v[name] = f
                    if (name.substr(-7) === 'capture') name = name.substring(0, a.length - 7)
                    if (!root[name]) root[name] = eventProxy
                } else {
                    var ns = isSvg && (name !== (name = name.replace(XLINK, '')))
                    if (value == null || value === false) {
                        if (ns) node.removeAttributeNS('http://www.w3.org/1999/xlink', name.toLowerCase())
                        else node.removeAttribute(name)
                    }
                    else if (type !== 'function') {
                        if (ns) node.setAttributeNS('http://www.w3.org/1999/xlink', name.toLowerCase(), value)
                        else node.setAttribute(name, value)
                    }
                }
            }
        }
    """)
}
