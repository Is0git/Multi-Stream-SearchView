package com.multistream.multistreamsearchview.filter

import android.view.View

class FilterSelection<T>(
    var dataName: String? = "Empty",
    var selectionListener: OnSelectionListener<T>? = null
) {

    var id: Int? = null

    var isEnabled: Boolean = false

    var isAllFilter: Boolean = false

    interface OnSelectionListener<T> {
        suspend fun getData(data: List<T>): List<T>
    }

    @Suppress("UNCHECKED_CAST")
    class Builder {
        private var filterSelectionName: String? = null

        fun setFilterSelectionName(name: String): Builder {
            filterSelectionName = name
            return this
        }


        fun <T> build(clazz: Class<T>, onSelect: (List<T>) -> List<T>): FilterSelection<T> {
            return FilterSelection<T>(
                filterSelectionName ?: "null",
                object : OnSelectionListener<T> {
                    override suspend fun getData(data: List<T>): List<T> {
                        return onSelect(data)
                    }

                }
            ).also { it.id = View.generateViewId() }
        }
    }
}