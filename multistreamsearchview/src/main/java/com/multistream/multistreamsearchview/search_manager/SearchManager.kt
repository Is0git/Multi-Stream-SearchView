package com.multistream.multistreamsearchview.search_manager

import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.filter.FilterSelection
import com.multistream.multistreamsearchview.filter.SearchDataFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchManager<T>() : FilterSelection.OnSelectionListener<T> {

    var itemsLiveData: MutableLiveData<List<T>>? = MutableLiveData()
    val filters: MutableList<SearchDataFilter<T>> by lazy { mutableListOf<SearchDataFilter<T>>() }
    var dataSource: DataSource<T> = DataSource()
    var isSourceDownloadEnabled = true
    var onQueryListener: OnQueryListener? = null
    var isShuffled = true

    constructor(onQueryListener: OnQueryListener) : this() {
        this.onQueryListener = onQueryListener
    }

    private suspend fun loadData(data: List<T>? = null) {
        withContext(Dispatchers.Main) { onQueryListener?.onDataLoad() }
        dataSource.getAllData()
    }

    fun addFilter(
        name: String,
        filterSelections: Collection<FilterSelection<T>>,
        isSingleSelection: Boolean,
        isAllSelectionEnabled: Boolean,
        allName: String? = null
    ) {
        val filter = SearchDataFilter<T>(
            name
        ).apply {
            if (isAllSelectionEnabled) {
                this.isMultipleSelectionEnabled = true
                addAllFilterSelection(this, allName)
            }
            this.isSingleSelection = isSingleSelection
            id = View.generateViewId()
            addFilterSelections(filterSelections)
        }
        filters.add(filter)
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<T>) {
        dataSource.addSourceDownloader(sourceDownloader)
    }

    suspend fun queryData(query: String, isQuickSearch: Boolean = false) {
        loadData()
        val message = "no data was loaded"
        if (dataSource.itemsData == null) {
            withContext(Dispatchers.Main) { onQueryListener?.onQueryCanceled(message) }
            throw CancellationException(message)
        }
        if (isShuffled) dataSource.itemsData = dataSource.itemsData!!.shuffled()
        if (isQuickSearch) {
            itemsLiveData?.postValue(dataSource.itemsData)
            withContext(Dispatchers.Main) {
                onQueryListener?.onQueryCompleted(
                    query,
                    isQuickSearch,
                    dataSource.itemsData!!.count()
                )
            }
            return
        }
        withContext(Dispatchers.Main) { onQueryListener?.onQueryFilter() }
        val result = filter(dataSource.itemsData!!)

        withContext(Dispatchers.Main) {
            onQueryListener?.onQueryCompleted(
                query,
                isQuickSearch,
                dataSource.itemsData!!.count()
            )
        }
        itemsLiveData?.postValue(result)
    }

    suspend fun filter(data: List<T>): List<T> {
        onQueryListener?.onQueryFilter()
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
        return filterData
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
        return filters.findInFilterOrNull { item: SearchDataFilter<T> ->
            var selection: FilterSelection<T>? = null
            for (filterSelection in item.filterSelections) {
                if (filterSelection.id == id) {
                    selection = filterSelection
                    break
                }
            }
            selection
        }
    }

    private fun addAllFilterSelection(
        filter: SearchDataFilter<T>,
        allName: String?
    ) {
        val filterSelection = FilterSelection<T>(
            allName ?: "All"
        ).apply {
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

interface OnQueryListener {
    fun onQueryStart()
    fun onDataLoad()
    fun onDataLoaded()
    fun onQueryFilter()
    fun onQueryCanceled(message: String)
    fun onQueryCompleted(query: String?, isQuickSearch: Boolean, count: Int)
}