package com.multistream.multistreamsearchview

import androidx.lifecycle.MutableLiveData

open class SearchDataFilter<T> {
    var filterName: String? = null

    var filterSelections: MutableList<SelectionData<T>>? = null
}