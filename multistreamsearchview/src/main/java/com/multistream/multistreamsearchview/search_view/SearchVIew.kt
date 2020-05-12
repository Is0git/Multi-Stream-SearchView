package com.multistream.multistreamsearchview.search_view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SearchView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.filter.FilterSelection
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.data_source.DataSourceAdapter
import com.multistream.multistreamsearchview.data_sync.MultipleSelectionDataSync
import com.multistream.multistreamsearchview.filter.FilterLayout
import com.multistream.multistreamsearchview.recent_search.RecentListAdapter
import com.multistream.multistreamsearchview.search_manager.OnQueryListener
import com.multistream.multistreamsearchview.search_manager.SearchManager
import com.multistream.multistreamsearchview.search_result.SearchListAdapter
import kotlinx.coroutines.*
import javax.xml.transform.Source


class SearchViewLayout : MotionLayout, SearchView.OnQueryTextListener,
    OnItemClickListener, OnQueryListener {

    companion object {
        val GAMES = 0
        val CHANNELS = 1
        val STREAMS = 2
    }

    lateinit var headlineText: MaterialTextView

    lateinit var searchView: SearchView

    lateinit var recentSearchesList: RecyclerView

    lateinit var recentListAdapter: RecentListAdapter

    lateinit var settingsButton: MaterialButton

    lateinit var dataSourceRecyclerView: RecyclerView

    lateinit var latestSearchesList: RecyclerView

    lateinit var latestSearchesText: MaterialTextView

    lateinit var searchProgressBar: SpinKitView

    lateinit var appBarLayout: AppBarLayout

    lateinit var motionLayout: com.multistream.multistreamsearchview.app_bar.AppBarLayout

    private var debounceLength = 1500L

    var searchManager = SearchManager<SearchData>(this)

    var headlineTextString: String = resources.getString(R.string.search)

    var initializeDefaultFilters: Boolean = false

    private var searchListAdapter = SearchListAdapter()

    var latestSearchesAdapter = LatestSearchedAdapter(
        listOf(
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg"
            ),
            SearchData(
                "Game of thrones",
                platform = 1,
                categoryStringId = R.string.games_category,
                platformResId = R.drawable.tune_icon,
                imageUrl = "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg"
            )
        )
    )

    var loadDataJob: Job? = null

    var quickQueryJob: Job? = null

    val multipleSelectionDataSync: MultipleSelectionDataSync<SearchData> by lazy { MultipleSelectionDataSync<SearchData>() }

    lateinit var searchRecyclerView: RecyclerView

    private var searchFocusTransition = TransitionInflater.from(context).inflateTransition(
        R.transition.on_search_focus_transition
    )

    private var filterExpandTransition = TransitionInflater.from(context).inflateTransition(
        R.transition.filters_expand
    )

    lateinit var filterLayout: FilterLayout

    var isFilterLayoutHidden = true

    var hiddenFilterLayoutHeight = 1

    var isDataSourceVisible = true

    var dataSourceAdapter: DataSourceAdapter? = null

    var selectedButtonColor = R.color.colorAccent

    var defaultButtonColor = R.color.colorSurface

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

    fun addFilter(
        name: String,
        filterSelections: Collection<FilterSelection<SearchData>>,
        isSingleSelection: Boolean = false,
        isAllSelectionEnabled: Boolean = true
    ) {
        filterLayout.addFilter(name, filterSelections, isSingleSelection, isAllSelectionEnabled)
    }

    fun addSourceDownloader(sourceDownloader: DataSource.SourceDownloader<SearchData>) {
        searchManager.addSourceDownloader(sourceDownloader)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query.isNullOrEmpty()) {
            return true
        } else {
            quickQueryJob?.cancel()
            loadDataJob?.cancel()
            loadDataJob = CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) { onQueryStart() }
                delay(debounceLength)
                filterLayout.query(query)
            }
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onType(newText)
        if (!newText.isNullOrBlank()) {
            if (quickQueryJob != null && quickQueryJob!!.isActive) quickQueryJob?.cancel()
            quickQueryJob = CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) { onQueryStart() }
                delay(debounceLength)
                searchManager.queryData(newText, false)
            }
        }
        return true
    }

    data class SearchData(
        var title: String? = null,
        var imageUrl: String? = null,
        var category: Int? = null,
        @StringRes var categoryStringId: Int,
        var platform: Int,
        @DrawableRes var platformResId: Int
    )

    private fun init(context: Context?, attrs: AttributeSet? = null) {
        searchManager.dataSource.addDefault(SearchData::class.java)
        filterLayout = FilterLayout(searchManager, context).apply {
            this.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorSurface, null
                )
            )
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).also {
                it.topToBottom = PARENT_ID
                it.marginStart = 20
                it.marginEnd = 20
            }
            setOnClickListener { filterAnimation() }
            elevation = convertDpToPixel(12, resources).toFloat()
        }
        filterLayout.getFilteredObserver()?.observe(
            (context as AppCompatActivity),
            Observer { searchListAdapter.searchData = it })
        attrs?.also {
            val typedArray = context?.obtainStyledAttributes(
                attrs,
                R.styleable.SearchViewLayout
            )
            headlineTextString = typedArray?.getString(R.styleable.SearchViewLayout_headlineText)
                ?: resources.getString(R.string.search)
            initializeDefaultFilters = typedArray?.getBoolean(
                R.styleable.SearchViewLayout_initializeDefaultFilters,
                false
            )!!
            typedArray.recycle()
        }
        val list = listOf(
            RecentListAdapter.RecentData(1, "greekgodx", 1589147066, 5),
            RecentListAdapter.RecentData(1, "yassuo", 1589147066, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 1589116706, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 1589116706, 10),
            RecentListAdapter.RecentData(1, "greekgodx", 1589116706, 5),
            RecentListAdapter.RecentData(1, "yassuo", 1589116706 - 90000, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 1589116706 - 90000, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 1589116706 - 90000, 10),
            RecentListAdapter.RecentData(1, "greekgodx", 1589116706 - 90000, 5),
            RecentListAdapter.RecentData(1, "yassuo", 1589116706 - 90000, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 1589116706 - 90000, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 1589116706 - 90000, 10),
            RecentListAdapter.RecentData(1, "greekgodx", 145, 5),
            RecentListAdapter.RecentData(1, "yassuo", 1589116706 - 36000000, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 1589116706 - 36000000, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 1589116706 - 36000000, 10)
        )
        recentListAdapter = RecentListAdapter(context, list, this)
        recentSearchesList = RecyclerView(context!!).apply {
            id = R.id.recent_searches_list
            setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorSurface, null
                )
            )
            visibility = View.GONE
            elevation = 10f
            layoutManager = LinearLayoutManager(context)
            adapter = recentListAdapter
        }
        addView(recentSearchesList)
        addView(filterLayout)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
        appBarLayout = coordinatorLayout.findViewById<AppBarLayout>(R.id.app_bar_layout)
        searchView = appBarLayout.findViewById<SearchView>(R.id.search_view).apply {
            this.setOnQueryTextListener(this@SearchViewLayout)
            setOnSearchClickListener {
                onSearch()
            }
            setOnCloseListener {
                onClose()
                false
            }
        }
        settingsButton = findViewById<MaterialButton>(R.id.settings_button).apply {
            this.setOnClickListener { filterAnimation() }
        }
        latestSearchesList = findViewById<RecyclerView>(R.id.searchedList).apply {
            adapter = latestSearchesAdapter
        }

        dataSourceRecyclerView = findViewById<RecyclerView>(R.id.data_source_list).apply {
        }
        headlineText = appBarLayout.findViewById(R.id.headline)
        recentSearchesList.layoutParams = LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            bottomToBottom = PARENT_ID
            topMargin = convertDpToPixel(50, resources)
            topToTop = PARENT_ID
        }
        searchRecyclerView = findViewById<RecyclerView>(R.id.search_list).apply {
            adapter = searchListAdapter
        }
        searchProgressBar = findViewById(R.id.progress_bar)
        motionLayout = appBarLayout.findViewById(R.id.motionLayout)
        dataSourceRecyclerView = motionLayout.findViewById(R.id.data_source_list)
        motionLayout.materialTextView = appBarLayout.findViewById(R.id.latest_search_text)
    }

    private fun initDataSource(sourceDownloads: MutableList<DataSource.SourceDownloader<SearchData>>) {
        dataSourceAdapter =
            DataSourceAdapter(sourceDownloads).also { sourceAdapter ->
                sourceAdapter.onItemClickListener = object : OnItemClickListener {
                    override fun onClick(position: Int, view: View) {
                        if (dataSourceAdapter?.dataSource?.get(position)!!.id == R.id.all_data_source) {
                            onDataSourceAllButtonClick(position, view)
                        } else {
                            onDataSourceButtonClick(position, view)
                        }
                    }
                }
                dataSourceRecyclerView.adapter = sourceAdapter
            }
    }

    fun onDataSourceButtonClick(position: Int, view: View) {
        handleButtonSelection(view, position, !view.isSelected)
        val allButton = getSourceButton(0)
        if (allButton != null) {
            handleButtonSelection(allButton, 0, false)
        }
    }

    fun onDataSourceAllButtonClick(position: Int, view: View) {
        view.also {
            handleButtonSelection(view, 0, true)
        }
        (1 until dataSourceAdapter?.dataSource?.count()!!).forEach {
            val unselectedView = getSourceButton(it)
            if (unselectedView != null) {
                handleButtonSelection(unselectedView, it, false)
            }
        }
    }

    private fun handleButtonSelection(view: View, position: Int, isSelected: Boolean) {
        view.isSelected = isSelected
        changeButtonState(view, isSelected)
        syncDataSourceData(position, isSelected)
    }

    private fun getSourceButton(position: Int): MaterialCardView? {
        return dataSourceRecyclerView.layoutManager?.getChildAt(position)
            ?.findViewById(R.id.sourceButton)
    }

    private fun changeButtonState(view: View, isEnabled: Boolean) {
        val colorId = if (isEnabled) {
            selectedButtonColor
        } else {
            defaultButtonColor
        }
        val color = ResourcesCompat.getColor(resources, colorId, null)
        val colorStateList = ColorStateList.valueOf(color)
        ViewCompat.setBackgroundTintList(view, colorStateList)

    }

    private fun onSearch() {
        TransitionManager.beginDelayedTransition(this, searchFocusTransition)
        recentSearchesList.visibility = View.VISIBLE
        searchView.layoutParams = (searchView.layoutParams as LayoutParams).apply {
            startToStart = PARENT_ID
            endToStart = settingsButton.id
            topToTop = PARENT_ID
            topMargin = 0
            width = MATCH_CONSTRAINT
        }
        motionLayout.getConstraintSet(R.id.start).apply {
            getConstraint(R.id.search_view).layout.apply {
                topToTop = PARENT_ID
                topToBottom = UNSET
                endToStart = R.id.settings_button
                startToStart = PARENT_ID
                this.mWidth = MATCH_CONSTRAINT
                topMargin = 0
            }

            getConstraint(R.id.settings_button).layout.apply {
                startToEnd = UNSET
                endToEnd = PARENT_ID
            }
        }
    }

    private fun onClose() {
        quickQueryJob?.cancel()
        searchProgressBar.visibility = View.INVISIBLE
        TransitionManager.beginDelayedTransition(this, searchFocusTransition)
        recentSearchesList.visibility = View.GONE
        motionLayout.getConstraintSet(R.id.start).apply {
            getConstraint(R.id.search_view).layout.apply {
                topToTop = UNSET
                topToBottom = R.id.headline
                endToStart = UNSET
                startToStart = R.id.headline
                this.mWidth = convertDpToPixel(50, resources)
                topMargin = convertDpToPixel(50, resources)
            }

            getConstraint(R.id.settings_button).layout.apply {
                startToEnd = R.id.search_view
                endToEnd = UNSET
            }
        }
    }

    private fun onType(text: String?) {
        if (text.isNullOrBlank()) {
            recentSearchesList.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.INVISIBLE
        } else {
            recentSearchesList.visibility = View.INVISIBLE
            searchRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onClick(position: Int, view: View) {
        searchView.setQuery(recentListAdapter.recentData?.get(position)?.searchText, true)
    }

    @Suppress("UNCHECKED_CAST")
    fun initSearchView() {
        filterLayout.createFilters()
        initDataSource(searchManager.dataSource.sourceDownloads as MutableList<DataSource.SourceDownloader<SearchData>>)
    }

    private fun filterAnimation() {
        TransitionManager.beginDelayedTransition(this, filterExpandTransition)
        if (isFilterLayoutHidden) {
            filterLayout.layoutParams = (filterLayout.layoutParams as LayoutParams).apply {
                topToBottom = UNSET
                bottomToBottom = PARENT_ID
                isFilterLayoutHidden = false
            }
        } else {
            filterLayout.layoutParams = (filterLayout.layoutParams as LayoutParams).apply {
                topToBottom = PARENT_ID
                bottomToBottom = UNSET
                isFilterLayoutHidden = true
            }
        }
    }

    override fun onQueryStart() {
        searchProgressBar.visibility = View.VISIBLE
        searchListAdapter.searchData = null
    }

    override fun onDataLoad() {

    }

    override fun onDataLoaded() {

    }

    override fun onQueryFilter() {

    }

    override fun onQueryCanceled(message: String) {
        searchProgressBar.visibility = View.INVISIBLE
    }

    override fun onQueryCompleted() {
        searchProgressBar.visibility = View.INVISIBLE
    }

    private fun syncDataSourceData(position: Int, isSelected: Boolean) {
        dataSourceAdapter?.dataSource?.get(position)?.isEnabled = isSelected
    }
}

interface OnItemClickListener {
    fun onClick(position: Int, view: View)
}

fun convertDpToPixel(dp: Int, resources: Resources): Int {
    return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}