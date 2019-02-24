package net.mengb.warpin

fun isArray(obj: dynamic) = js("Boolean(obj) && 'length' in obj") as Boolean
