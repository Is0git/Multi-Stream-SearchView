package com.multistream.multistreamsearchview.filter

open class SearchDataFilter<T>(var filterName: String? = null) {
    var id: Int = 0

    var isSingleSelection: Boolean = false

    var isMultipleSelectionEnabled = true

    val filterSelections: MutableList<FilterSelection<T>> by lazy { mutableListOf<FilterSelection<T>>() }

    fun addFilterSelections(filterSelections: Collection<FilterSelection<T>>) {
        this.filterSelections.addAll(filterSelections)
    }

    fun addFilterSelection(filterSelection: FilterSelection<T>) {
        this.filterSelections.add(filterSelection)
    }

    fun enableAll() {
        filterSelections.forEach { it.isEnabled = true }
    }
}