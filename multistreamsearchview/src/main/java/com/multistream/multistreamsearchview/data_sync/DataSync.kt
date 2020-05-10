package com.multistream.multistreamsearchview.data_sync

import com.multistream.multistreamsearchview.filter.FilterSelection

abstract class DataSync<T> {

    fun sync(filterSelection: FilterSelection<T>, selectedFilterId: Int, isSelected: Boolean) {
                syncFilter(filterSelection, selectedFilterId, isSelected)
    }

    abstract fun syncFilter(filterSelection: FilterSelection<T>, selectedFilterSelectionId: Int, isSelected: Boolean)
}