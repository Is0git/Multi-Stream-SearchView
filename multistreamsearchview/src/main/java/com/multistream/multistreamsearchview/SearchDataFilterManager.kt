package com.multistream.multistreamsearchview

import androidx.lifecycle.MutableLiveData

class SearchFilterManager<T> {

    var itemsData: List<T>? = null

    var itemsLiveData: MutableLiveData<List<T>>? = null

    var filters: MutableList<SearchDataFilter<T>>? = null
}