package com.multistream.multistreamsearchview.search_view

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SearchView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.filter.FilterSelection
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.data_source.DataSourceAdapter
import com.multistream.multistreamsearchview.data_sync.MultipleSelectionDataSync
import com.multistream.multistreamsearchview.filter.FilterLayout
import com.multistream.multistreamsearchview.recent_search.RecentListAdapter
import com.multistream.multistreamsearchview.search_manager.SearchManager
import com.multistream.multistreamsearchview.search_result.SearchListAdapter
import kotlinx.coroutines.*


class SearchViewLayout : ConstraintLayout, SearchView.OnQueryTextListener,
    OnItemClickListener {

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

    var debounceLength = 1000L

    var searchManager = SearchManager<SearchData>()

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
        loadDataJob =
            CoroutineScope(Dispatchers.Default).launch { filterLayout.query(query!!) }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onType(newText)
        if (!newText.isNullOrBlank()) {
            if (quickQueryJob != null && quickQueryJob!!.isActive) quickQueryJob?.cancel()
                quickQueryJob = CoroutineScope(Dispatchers.Default).launch {
                    delay(debounceLength)
                    searchManager.queryData(newText, true)
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
        setBackground(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bg_gradient, null
            )
        )
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
            elevation = 10f
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
        headlineText = MaterialTextView(getContext()).apply {
            id = R.id.search_text
            text = resources.getString(R.string.search)
            typeface = Typeface.DEFAULT_BOLD
            TextViewCompat.setTextAppearance(
                this,
                R.style.TextAppearance_MaterialComponents_Headline3
            )
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                marginStart =
                    convertDpToPixel(
                        35,
                        resources
                    )
                marginEnd =
                    convertDpToPixel(
                        35,
                        resources
                    )
                topMargin =
                    convertDpToPixel(
                        50,
                        resources
                    )
                endToEnd = PARENT_ID
            }
            alpha = 0.5f
        }
        searchView = SearchView(context).apply {
            id = R.id.search_view
            layoutParams = LayoutParams(MATCH_CONSTRAINT, 170).apply {
                endToStart =
                    R.id.settings_button
                topToBottom = headlineText.id
                startToStart = headlineText.id
                topMargin =
                    convertDpToPixel(
                        30,
                        resources
                    )
            }
            background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_search_view, null
            )
            val backgroundView = this.findViewById(androidx.appcompat.R.id.search_plate) as View?
            backgroundView?.background = null
            elevation = 30f
            this.setOnQueryTextListener(this@SearchViewLayout)
        }

        settingsButton = MaterialButton(context!!).apply {
            id = R.id.settings_button
            val dp =
                convertDpToPixel(
                    50,
                    resources
                )
            layoutParams = LayoutParams(dp, MATCH_CONSTRAINT).also {
                it.topToTop = searchView.id
                it.bottomToBottom = searchView.id
                it.endToEnd = headlineText.id
            }
            this.cornerRadius = 8
            this.setIconResource(R.drawable.tune_icon)
            this.iconSize =
                convertDpToPixel(
                    25,
                    resources
                )
            this.setIconTintResource(R.color.colorOnSurface)
            this.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            this.iconPadding = 0
            this.elevation = 30f
            this.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorSurface, null
                )
            )

            setOnClickListener { filterAnimation() }
        }
        dataSourceRecyclerView = RecyclerView(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                this.topToBottom = searchView.id
                this.startToStart = headlineText.id
                this.endToEnd = headlineText.id
                this.topMargin = convertDpToPixel(20, resources)
            }
            scrollBarSize = 0
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutManager =
                LinearLayoutManager(context).also { it.orientation = RecyclerView.HORIZONTAL }
        }
        searchRecyclerView = RecyclerView(context).apply {
            setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorLight, null))
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_CONSTRAINT).also {
                it.bottomToBottom = PARENT_ID
                it.topMargin = convertDpToPixel(2, resources)
                it.topToBottom = R.id.search_view
            }
            visibility = View.INVISIBLE
            layoutManager = LinearLayoutManager(context)
            elevation = 10f
            adapter = searchListAdapter
        }
        latestSearchesText = MaterialTextView(getContext()).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                topToBottom = dataSourceRecyclerView.id
                startToStart = headlineText.id
                endToEnd = headlineText.id
                topMargin = convertDpToPixel(25, resources)
            }
            maxLines = 1
            typeface = Typeface.DEFAULT_BOLD
            text = getContext().getString(R.string.latest_searches)
            TextViewCompat.setTextAppearance(
                this,
                R.style.TextAppearance_MaterialComponents_Headline5
            )
        }
        latestSearchesList = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT).apply {
                topToBottom = latestSearchesText.id
                startToStart = headlineText.id
                endToEnd = headlineText.id
                bottomToBottom = PARENT_ID
                topMargin = convertDpToPixel(10, resources)
            }
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            adapter = latestSearchesAdapter
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
        recentSearchesList = RecyclerView(context).apply {
            id = View.generateViewId()
            setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorSurface, null
                )
            )
            layoutParams = LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT).apply {
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
                bottomToBottom = PARENT_ID
                topToBottom = searchView.id
            }
            visibility = View.GONE
            elevation = 10f
            layoutManager = LinearLayoutManager(context)
            adapter = recentListAdapter
        }
        addView(settingsButton)
        addView(headlineText)
        addView(searchView)
        addView(searchRecyclerView)
        addView(recentSearchesList)
        addView(dataSourceRecyclerView)
        addView(latestSearchesText)
        addView(latestSearchesList)
        addView(filterLayout)
        searchView.setOnSearchClickListener {
            onSearch()
        }
        searchView.setOnCloseListener {
            onClose()
            false
        }
    }

    private fun initDataSource(sourceDownloads: MutableList<DataSource.SourceDownloader<SearchData>>) {
        dataSourceAdapter =
            DataSourceAdapter(sourceDownloads).also { dataSourceRecyclerView.adapter = it }
    }

    private fun onSearch() {
        TransitionManager.beginDelayedTransition(this, searchFocusTransition)
        recentSearchesList.visibility = View.VISIBLE
        headlineText.visibility = View.INVISIBLE
        searchView.layoutParams = (searchView.layoutParams as LayoutParams).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            topToTop = PARENT_ID
            topMargin = 0
            width = MATCH_PARENT
        }
        searchView.background = ResourcesCompat.getDrawable(resources, R.color.colorSurface, null)
    }

    private fun onClose() {
        TransitionManager.beginDelayedTransition(this, searchFocusTransition)
        recentSearchesList.visibility = View.GONE
        headlineText.visibility = View.VISIBLE
        searchView.layoutParams = (searchView.layoutParams as LayoutParams).apply {
            startToStart = headlineText.id
            endToEnd = headlineText.id
            topToBottom = headlineText.id
            topToTop = LayoutParams.UNSET
            topMargin =
                convertDpToPixel(
                    50,
                    resources
                )
            width = MATCH_CONSTRAINT
        }
        searchView.background =
            ResourcesCompat.getDrawable(resources, R.drawable.rounded_search_view, null)
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

    override fun onClick(position: Int) {
        searchView.setQuery(recentListAdapter.recentData?.get(position)?.searchText, true)
    }

    fun initSearchView() {
        filterLayout.createFilters()
        initDataSource(searchManager.dataSource.sourceDownloads)
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

}

interface OnItemClickListener {
    fun onClick(position: Int)
}

fun convertDpToPixel(dp: Int, resources: Resources): Int {
    return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}