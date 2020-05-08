package com.multistream.multistreamsearchview.data_sync

import com.multistream.multistreamsearchview.FilterSelection

class SingleSelectionDataSync<T> : DataSync<T>() {
    override fun syncFilter(
        filterSelection: FilterSelection<T>,
        selectedFilterSelectionId: Int,
        isSelected: Boolean
    ) {
        filterSelection.isEnabled = isSelected
    }
}