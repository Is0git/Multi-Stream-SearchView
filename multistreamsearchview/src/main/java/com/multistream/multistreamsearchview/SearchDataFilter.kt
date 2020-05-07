package com.multistream.multistreamsearchview

import androidx.lifecycle.MutableLiveData

open class SearchDataFilter<T>(var filterName: String? = null) {
    var id: Int = 0

    var isSingleSelection: Boolean = false

    var isMultipleSelectionEnabled = true

    val filterSelections: MutableList<SelectionData<T>> by lazy { mutableListOf<SelectionData<T>>() }

    fun addFilterSelections(filterSelections: Collection<SelectionData<T>>) {
        this.filterSelections.addAll(filterSelections)
    }

    fun addFilterSelection(filterSelection: SelectionData<T>) {
        this.filterSelections.add(filterSelection)
    }

    fun enableAll() {
        filterSelections.forEach { it.isEnabled = true }
    }
}