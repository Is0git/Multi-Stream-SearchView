package com.multistream.multistreamsearchview

import android.util.Log
import androidx.lifecycle.MutableLiveData

class SearchManager<T> {

    private var itemsData: List<T>? = null

    var itemsLiveData: MutableLiveData<List<T>>? = MutableLiveData()

    val filters: MutableList<SearchDataFilter<T>> by lazy { mutableListOf<SearchDataFilter<T>>() }

    var dataSource: DataSource<T>? = DataSource()

    var isSourceDownloadEnabled = true

    private suspend fun loadData(data: List<T>? = null): List<T>? {
        data?.let {
            itemsData = it
        }
        if (isSourceDownloadEnabled && dataSource != null) {
            itemsData = dataSource?.getAllData()
        }
        return itemsData
    }

    fun addFilter(
        name: String,
        filterSelections: Collection<SelectionData<T>>,
        isSingleSelection: Boolean,
        isMultipleSelectionEnabled: Boolean
    ) {
        val filter = SearchDataFilter<T>(name)
        filter.addFilterSelections(filterSelections)
        filter.isSingleSelection = isSingleSelection
        filter.isMultipleSelectionEnabled = isMultipleSelectionEnabled
        filters.add(filter)
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<T>) {
        dataSource?.addSourceDownloader(sourceDownloader)
    }

    suspend fun queryData(query: String) {
        val data = loadData()

        if (data != null && data.isNotEmpty()) {
            val filtered: MutableList<T> = mutableListOf<T>()
            for (i in filters.indices) {

                for (b in filters[i].filterSelections) {
                   val result = if (i == 0)  b.selectionListener?.getData(data) else b.selectionListener?.getData(filtered)
                    filtered.addAll(result!!)

                }

                if (searchData.isEmpty()) break
            }
            itemsLiveData?.postValue(itemsData)
        }
    }
}