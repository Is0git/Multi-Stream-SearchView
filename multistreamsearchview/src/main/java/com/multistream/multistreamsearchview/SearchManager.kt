package com.multistream.multistreamsearchview

import androidx.annotation.IdRes
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
        filterSelections: Collection<FilterSelection<T>>,
        isSingleSelection: Boolean,
        isAllSelectionEnabled: Boolean
    ) {
        val filter = SearchDataFilter<T>(name).apply {
            if (isAllSelectionEnabled) {
                this.isAllSelectionEnabled = true
                addAllFilterSelection(this)
                this.isSingleSelection = isSingleSelection
            }
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
        var filteredResult: MutableList<T>
        for (filter in filters) {
            val hasFilters = hasEnabledFilters(filter)
            if (hasFilters) {
                filteredResult = mutableListOf()
                for (filterSelection in filter.filterSelections) {
                    if (filterSelection.isEnabled) filterSelection.selectionListener?.getData(
                        filterData
                    )?.also {
                        filteredResult.addAll(it)
                    }
                }
            } else {
                continue
            }
            filterData = filteredResult
        }
        itemsLiveData?.postValue(filterData)
    }

    private fun hasEnabledFilters(filter: SearchDataFilter<T>): Boolean {
        val allFilter = filter.filterSelections.first()
        if (filter.filterSelections.first().id == R.id.default_chip && allFilter.isEnabled) {
            return false
        } else {
            for (i in filter.filterSelections) {
                if (i.id != R.id.default_chip && i.isEnabled) return true
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
        val filterSelection = FilterSelection<T>("All")
        filterSelection.id = R.id.default_chip
        filter.filterSelections.add(filterSelection)
    }
}

fun <T, R> List<T>.findInFilterOrNull(action: (item: T) -> R?): R? {
    var result: R? = null
    for (b in this) {
        result = action(b)
    }
    return result
}