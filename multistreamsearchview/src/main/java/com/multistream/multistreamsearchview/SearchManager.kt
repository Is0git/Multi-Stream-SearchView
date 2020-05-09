package com.multistream.multistreamsearchview

import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData

class SearchManager<T> : FilterSelection.OnSelectionListener<T> {

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
        filterSelections: Collection<FilterSelection<T>>,
        isSingleSelection: Boolean,
        isAllSelectionEnabled: Boolean
    ) {
        val filter = SearchDataFilter<T>(name).apply {
            if (isAllSelectionEnabled) {
                this.isMultipleSelectionEnabled = true
                addAllFilterSelection(this)
                this.isSingleSelection = isSingleSelection
            }
            id = View.generateViewId()
            addFilterSelections(filterSelections)
        }
        filters.add(filter)
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<T>) {
        dataSource?.addSourceDownloader(sourceDownloader)
    }

    suspend fun queryData(query: String) {
        val data = loadData() ?: return
        var filterData = data
        for (filter in filters) {
            val filteredResult: MutableList<T> = mutableListOf()
            for (filterSelection in filter.filterSelections) {
                if (filterSelection.isEnabled) {
                    val result = filterSelection.selectionListener?.getData(filterData)
                    filteredResult.addAll(result!!)
                }
            }
            filterData = filteredResult
            if (filterData.isEmpty()) break
        }
        itemsLiveData?.postValue(filterData)
    }

    private fun hasEnabledFilters(filter: SearchDataFilter<T>): Boolean {
        val allFilter = filter.filterSelections.first()
        if (allFilter.isAllFilter && allFilter.isEnabled) {
            return false
        } else {
            for (i in filter.filterSelections) {
                if (!i.isAllFilter && i.isEnabled) return true
            }
        }
        return false
    }

    fun findSelectionFilterById(@IdRes id: Int): FilterSelection<T>? {
        var result = filters.findInFilterOrNull { item: SearchDataFilter<T> ->
            var selection: FilterSelection<T>? = null
            for (filterSelection in item.filterSelections) {
                if (filterSelection.id == id) {
                    selection = filterSelection
                    break
                }
            }
            selection
        }
        return result
    }

    private fun addAllFilterSelection(filter: SearchDataFilter<T>) {
        val filterSelection = FilterSelection<T>("All").apply {
            id = View.generateViewId()
            isAllFilter = true
            isEnabled = true
            selectionListener = this@SearchManager
        }
        filter.filterSelections.add(filterSelection)
    }

    override suspend fun getData(data: List<T>): List<T> {
        return data
    }

    fun findFilterBySelectionId(id: Int): SearchDataFilter<T>? {
        return filters.find { dataFilter ->
            val selectionData = dataFilter.filterSelections.firstOrNull { it.id == id }
            selectionData != null
        }
    }
}
fun <T, R> List<T>.findInFilterOrNull(action: (item: T) -> R?): R? {
    var result: R? = null
    for (b in this) {
        result = action(b)
        if (result != null) break
    }
    return result
}