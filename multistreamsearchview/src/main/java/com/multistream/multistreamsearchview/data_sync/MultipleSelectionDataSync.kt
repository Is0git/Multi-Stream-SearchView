package com.multistream.multistreamsearchview.data_sync

import com.multistream.multistreamsearchview.filter.FilterSelection

class MultipleSelectionDataSync<T> : DataSync<T>() {
    override fun syncFilter(
        filter: FilterSelection<T>,
        selectedFilterSelectionId: Int,
        isSelected: Boolean
    ) {
        filter.isEnabled = isSelected
    }
}