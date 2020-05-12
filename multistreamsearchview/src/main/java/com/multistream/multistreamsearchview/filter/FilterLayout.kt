package com.multistream.multistreamsearchview.filter

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.LiveData
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.*
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.data_sync.SingleSelectionDataSync
import com.multistream.multistreamsearchview.search_manager.SearchManager
import com.multistream.multistreamsearchview.search_view.SearchViewLayout
import com.multistream.multistreamsearchview.search_view.convertDpToPixel

class FilterLayout : ConstraintLayout, CompoundButton.OnCheckedChangeListener {

    private var searchFilterManager: SearchManager<SearchViewLayout.SearchData>? = null

    var defaultFilterName = resources.getString(R.string.default_filter_name)

    var filtersMarginTop =
        convertDpToPixel(
            25,
            resources
        )

    lateinit var filterText: TextView

    private val singleSelectionDataSync: SingleSelectionDataSync<SearchViewLayout.SearchData> by lazy { SingleSelectionDataSync<SearchViewLayout.SearchData>() }

    constructor(context: Context?) : super(context) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {init(context)}

    constructor(searchManager: SearchManager<SearchViewLayout.SearchData>, context: Context? ) : super(context) {
        searchFilterManager = searchManager
        init(context)
    }

    private fun createAllFilterViews() {
        searchFilterManager?.filters?.forEach { searchDataFilter ->
            val filterHeadline =
                createFilterHeadline(searchDataFilter.filterName ?: defaultFilterName)
            addView(filterHeadline)
            val chipGroup = createChipGroup(
                filterHeadline,
                searchDataFilter.isSingleSelection,
                searchDataFilter.isMultipleSelectionEnabled
            )
            chipGroup.id = searchDataFilter.id
            searchDataFilter.filterSelections.forEach { selectionData ->
                createChip(
                    selectionData.dataName,
                    selectionData.id ?: View.generateViewId(),
                    searchDataFilter.isSingleSelection
                ).also {
                    chipGroup.addView(it)
                    it.isChecked = selectionData.isEnabled
                    it.setOnCheckedChangeListener(this)
                }
            }
            addView(chipGroup)
        }
    }

    private fun createFilterHeadline(title: String): TextView {
        val lastChildView = getChildAt(childCount - 1)
        val newLayoutParams = LayoutParams(
            convertDpToPixel(
                100,
                resources
            ), LayoutParams.WRAP_CONTENT).also {
            it.topToBottom = lastChildView.id
            it.startToStart = filterText.id
            it.topMargin = filtersMarginTop
        }
        val text = MaterialTextView(context, null, R.attr.textAppearanceBody1).apply {
            id = View.generateViewId()
            text = title
            layoutParams = newLayoutParams
        }
        TextViewCompat.setTextAppearance(text, R.style.TextAppearance_MaterialComponents_Body1)
        return text
    }

    private fun createChipGroup(
        titleView: View,
        isSingleSelection: Boolean,
        isAllSelectionEnabled: Boolean
    ): ChipGroup {
        return ChipGroup(context, null, R.attr.ChipAction).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_CONSTRAINT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.startToEnd = titleView.id
                it.topToTop = titleView.id
                it.endToEnd = PARENT_ID
            }
            this.isSingleSelection = isSingleSelection
        }
    }

    private fun createChip(title: String?, id: Int, isSingleSelection: Boolean): Chip {
        return Chip(context).apply {
            this.id = id
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = title
            isCheckedIconVisible = isSingleSelection
            isCheckable = true
        }
    }

    fun addFilter(
        name: String,
        filterSelections: Collection<FilterSelection<SearchViewLayout.SearchData>>,
        isSingleSelection: Boolean = false,
        isAllSelectionEnabled: Boolean = true
    ) {
        searchFilterManager?.addFilter(
            name,
            filterSelections,
            isSingleSelection,
            isAllSelectionEnabled
        )
    }


    private fun syncChipGroupWithData(@IdRes selectedId: Int, isChecked: Boolean) {
        val filterSelection = searchFilterManager?.findSelectionFilterById(selectedId)
        if (filterSelection != null) {
            val filter = searchFilterManager?.findFilterBySelectionId(selectedId)
            if (filter != null && filter.isMultipleSelectionEnabled && filterSelection.isAllFilter) {
                val chipGroup = findViewById<ChipGroup>(filter.id)
                singleSelectionDataSync.sync(filterSelection, selectedId, isChecked)
                if (isChecked) {
                    for (chip in chipGroup.children) {
                        if (chip.id != selectedId) {
                            (chip as Chip).isChecked = false
                        }
                    }
                }
            } else {
                singleSelectionDataSync.sync(filterSelection, selectedId, isChecked)
            }
        } else {
            Log.e("filter", "no filter found")
        }
    }

    fun getFilteredObserver(): LiveData<List<SearchViewLayout.SearchData>>? {
        return searchFilterManager?.itemsLiveData
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<SearchViewLayout.SearchData>) {
        searchFilterManager?.addSourceDownloader(sourceDownloader)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        Log.d("MATCHID", "selected id: ${buttonView?.id}")
        buttonView?.let { syncChipGroupWithData(it.id, isChecked) }
    }

    fun invalidateFilters() {
        resolveFilterViewsStartPosition()
//        if (childCount > 0) {
//            if (childCount - 1 > filterViewsStartPosition) {
//                for (filter in searchFilterManager.filters) {
//                    removeViewAt(filter.id)
//                }
//            }
//
//        }
        createAllFilterViews()
    }

    private fun resolveFilterViewsStartPosition() {
//        children.forEachIndexed lit@{ index, view ->
//            if (view.id == filterDividerViewId) {
//                filterViewsStartPosition = index
//                return@lit
//            }
//        }
    }

    fun createFilters() {
        createAllFilterViews()
    }
    //    private fun createDataSourceChipGroup(): ChipGroup {
//        val titleView = createFilterHeadline("Choose something")
//        addView(titleView)
//        val chipGroup = createChipGroup(
//            titleView,
//            isSingleSelection = true,
//            isAllSelectionEnabled = true
//        )
//        searchFilterManager.dataSource?.sourceDownloads?.forEach {
//            val chip = createChip(it.isEnabled.toString(), View.generateViewId(), true)
//            chipGroup.addView(chip)
//        }
//        return chipGroup
//    }

   suspend fun query(text: String) {
        searchFilterManager?.queryData(text)
    }

    private fun init(context: Context?) {
        filterText = MaterialTextView(context!!).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).also {
                it.topToTop = PARENT_ID
                it.startToStart = PARENT_ID
                it.endToEnd = PARENT_ID
                it.topMargin =
                    convertDpToPixel(
                        30,
                        resources
                    )
                it.marginEnd=
                    convertDpToPixel(
                        25,
                        resources
                    )
                it.marginStart =
                    convertDpToPixel(
                        25,
                        resources
                    )
            }
            TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Headline3)
            text = context.getString(R.string.filters)
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }

        addView(filterText)
    }
}