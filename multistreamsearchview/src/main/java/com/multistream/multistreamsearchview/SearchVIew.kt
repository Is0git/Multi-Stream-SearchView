package com.multistream.multistreamsearchview

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CompoundButton
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.data_sync.MultipleSelectionDataSync
import com.multistream.multistreamsearchview.data_sync.SingleSelectionDataSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SearchViewLayout : ConstraintLayout, SearchView.OnQueryTextListener,
    CompoundButton.OnCheckedChangeListener {

    lateinit var headlineText: MaterialTextView

    lateinit var searchView: SearchView

    var searchFilterManager: SearchManager<SearchData> = SearchManager()

    var filtersMarginTop = convertDpToPixel(25)

    var headlineTextString: String = resources.getString(R.string.search)

    var defaultFilterName = resources.getString(R.string.default_filter_name)

    var initializeDefaultFilters: Boolean = false

    var filterDividerViewId = R.id.search_view

    var filterViewsStartPosition = 0

    var showDownloaderView: Boolean = true

    var searchListAdapter = SearchListAdapter()

    var loadDataJob: Job? = null

    val singleSelectionDataSync: SingleSelectionDataSync<SearchData> by lazy { SingleSelectionDataSync<SearchData>() }

    val multipleSelectionDataSync: MultipleSelectionDataSync<SearchData> by lazy { MultipleSelectionDataSync<SearchData>() }

    lateinit var recyclerView: RecyclerView

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun createAllFilterViews() {
        if (showDownloaderView) {
//            val chipGroup = createDataSourceChipGroup()
////            addView(chipGroup)
        }
        searchFilterManager.filters.forEach { searchDataFilter ->
            val filterHeadline =
                createFilterHeadline(searchDataFilter.filterName ?: defaultFilterName)
            addView(filterHeadline)
            val chipGroup = createChipGroup(
                filterHeadline,
                searchDataFilter.isSingleSelection,
                searchDataFilter.isAllSelectionEnabled
            )
            searchDataFilter.id = chipGroup.id
            searchDataFilter.filterSelections.forEach { selectionData ->
                createChip(
                    selectionData.dataName,
                    selectionData.id ?: View.generateViewId(),
                    searchDataFilter.isSingleSelection
                ).also {
                    Log.d("MATCHID", it.id.toString())
                    selectionData.id = it.id
                    chipGroup.addView(it)
                }
            }
            addView(chipGroup)
        }
        addView(recyclerView)
    }

    private fun createFilterHeadline(title: String): TextView {
        val lastChildView = getChildAt(childCount - 1)
        val newLayoutParams = LayoutParams(convertDpToPixel(100), LayoutParams.WRAP_CONTENT).also {
            it.topToBottom = lastChildView.id
            it.startToStart = searchView.id
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
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).also {
                id = View.generateViewId()
                it.startToEnd = titleView.id
                it.topToTop = titleView.id
                it.endToEnd = searchView.id
            }
            this.isSingleSelection = isSingleSelection
        }
    }

    private fun createChip(title: String?, id: Int, isSingleSelection: Boolean): Chip {
        return Chip(context).apply {
            this.id = id
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            text = title
            this.setOnCheckedChangeListener(this@SearchViewLayout)
            isCheckedIconVisible = isSingleSelection
            isCheckable = true
        }
    }

    fun addFilter(
        name: String,
        filterSelections: Collection<FilterSelection<SearchData>>,
        isSingleSelection: Boolean = false,
        isAllSelectionEnabled: Boolean = true
    ) {
        searchFilterManager.addFilter(
            name,
            filterSelections,
            isSingleSelection,
            isAllSelectionEnabled
        )
    }

    fun invalidateFilters() {
        resolveFilterViewsStartPosition()
        if (childCount > 0) {
            if (childCount - 1 > filterViewsStartPosition) {
                for (filter in searchFilterManager.filters) {
                    removeViewAt(filter.id)
                }
            }
            createAllFilterViews()
        }
    }

    private fun resolveFilterViewsStartPosition() {
        children.forEachIndexed lit@{ index, view ->
            if (view.id == filterDividerViewId) {
                filterViewsStartPosition = index
                return@lit
            }
        }
    }

    private fun createDataSourceChipGroup(): ChipGroup {
        val titleView = createFilterHeadline("Choose something")
        addView(titleView)
        val chipGroup = createChipGroup(
            titleView,
            isSingleSelection = true,
            isAllSelectionEnabled = true
        )
        searchFilterManager.dataSource?.sourceDownloads?.forEach {
            val chip = createChip(it.isEnabled.toString(), View.generateViewId(),true)
            chipGroup.addView(chip)
        }
        return chipGroup
    }

    private fun syncChipGroupWithData(@IdRes selectedId: Int, isChecked: Boolean) {
        val filterSelection = searchFilterManager.findSelectionFilterById(selectedId)
        filterSelection?.let {
        singleSelectionDataSync.sync(filterSelection, selectedId, isChecked)
        } ?: Log.e("filter", "no filter found")

        searchFilterManager.filters[0].filterSelections.forEach {
            Log.d("filter", "filter: ${it.dataName} enabled: ${it.isEnabled}")

        }
        Log.d("filter", "-------")
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<SearchData>) {
        searchFilterManager.addSourceDownloader(sourceDownloader)
    }

    private fun convertDpToPixel(dp: Int): Int {
        return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        loadDataJob =
            CoroutineScope(Dispatchers.Default).launch { searchFilterManager.queryData(query!!) }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    data class SearchData(
        var title: String? = null,
        var imageUrl: String? = null,
        var category: Int? = null,
        var platform: Int
    )

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        Log.d("MATCHID", "selected id: ${buttonView?.id}")
        buttonView?.let { syncChipGroupWithData(it.id, isChecked) }
    }

    private fun init(context: Context?, attrs: AttributeSet? = null) {
        searchFilterManager.itemsLiveData?.observe(
            (context as AppCompatActivity),
            Observer { searchListAdapter.data = it })

        attrs?.also {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.SearchViewLayout)
            headlineTextString = typedArray?.getString(R.styleable.SearchViewLayout_headlineText)
                ?: resources.getString(R.string.search)
            initializeDefaultFilters = typedArray?.getBoolean(
                R.styleable.SearchViewLayout_initializeDefaultFilters,
                false
            )!!
            typedArray.recycle()
        }
        headlineText = MaterialTextView(getContext()).apply {
            id = R.id.search_text
            text = headlineTextString
            TextViewCompat.setTextAppearance(
                this,
                R.style.TextAppearance_MaterialComponents_Headline4
            )
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                marginStart = convertDpToPixel(25)
                marginEnd = convertDpToPixel(25)
                topMargin = convertDpToPixel(30)
                endToEnd = PARENT_ID
            }
        }
        searchView = SearchView(context).apply {
            id = R.id.search_view
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                endToEnd = headlineText.id
                topToBottom = headlineText.id
                startToStart = headlineText.id
                topMargin = convertDpToPixel(25)
            }
            this.setOnQueryTextListener(this@SearchViewLayout)
        }
        recyclerView = RecyclerView(context!!).apply {
            layoutParams = LayoutParams(MATCH_PARENT, 950).also {
                it.bottomToBottom = PARENT_ID
            }
            layoutManager = LinearLayoutManager(context)
            adapter = searchListAdapter
        }
        addView(headlineText)
        addView(searchView)
        resolveFilterViewsStartPosition()
    }
}