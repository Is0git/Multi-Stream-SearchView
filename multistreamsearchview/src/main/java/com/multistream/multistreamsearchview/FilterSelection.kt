package com.multistream.multistreamsearchview

class FilterSelection<T>(var dataName: String? = "Empty", var selectionListener: OnSelectionListener<T>? = null) {

    var id: Int? = null

    var isEnabled: Boolean = false

    var default:

    interface OnSelectionListener<T> {
       suspend fun getData(data: List<T>) : List<T>
    }

    @Suppress("UNCHECKED_CAST")
    class Builder {
        private var filterSelectionName: String? = null

        private var selectionListener :OnSelectionListener<*>? = null

        fun setFilterSelectionName(name: String) : Builder {
            filterSelectionName = name
            return this
        }

        fun addSelectionListener(listener: OnSelectionListener<*>) : Builder {
            selectionListener = listener
            return this
        }

        fun<T> build(clazz: Class<T>) : FilterSelection<T> {
           return FilterSelection<T>(filterSelectionName ?: "null", selectionListener as OnSelectionListener<T>)
        }
    }
}