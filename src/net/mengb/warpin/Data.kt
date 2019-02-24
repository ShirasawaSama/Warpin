package net.mengb.warpin

class Data(val data: dynamic, var renderList: dynamic) {
    val __elements = js("new Set")
    val __cache = js("{}")
    operator fun set(key: Any?, value: Any?) {
        if (value is Data) {
            data[key] = value.data
            __cache[key] = value
        } else {
            data[key] = value
            __cache[key] = Data(value, renderList)
        }
        renderList.push(__elements)
    }
    operator fun invoke(): dynamic = data

    fun <T> to(): T {
        if (data is T) throw ClassCastException("Cannot convert $data to type.")
        return data as T
    }
    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T> toP(name: String): T {
        if (data !is T) throw ClassCastException("Cannot convert $data to $name.")
        return data
    }
    fun isNull(key: Any) = js("this.data[key] == null") as Boolean
    fun string() = toP<String>("String")
    fun int() = toP<Int>("Int")
    fun boolean() = toP<Boolean>("Boolean")
    fun double() = toP<Double>("Double")
    fun float() = toP<Float>("Float")
    fun byte() = toP<Byte>("Byte")
    fun long() = toP<Long>("Long")
    fun <T> array() = toP<Array<T>>("Array")
    fun <T> list() = toP<List<T>>("List")
    fun <T, C> map() = toP<Map<T, C>>("Map")
    fun <T> set() = toP<Set<T>>("Set")
}
