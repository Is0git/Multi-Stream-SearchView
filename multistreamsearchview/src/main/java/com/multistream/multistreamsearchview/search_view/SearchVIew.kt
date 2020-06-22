package com.multistream.multistreamsearchview.search_view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SearchView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.data_source.DataSourceAdapter
import com.multistream.multistreamsearchview.filter.FilterLayout
import com.multistream.multistreamsearchview.filter.FilterSelection
import com.multistream.multistreamsearchview.recent_search.HistoryListAdapter
import com.multistream.multistreamsearchview.search_manager.OnQueryListener
import com.multistream.multistreamsearchview.search_manager.SearchManager
import com.multistream.multistreamsearchview.search_result.SearchListAdapter
import kotlinx.coroutines.*
import kotlin.math.min


class SearchViewLayout : MotionLayout, SearchView.OnQueryTextListener,
    OnItemClickListener, OnQueryListener, LifecycleObserver {
    companion object {
        const val GAMES = 0
        const val CHANNELS = 1
        const val STREAMS = 2
    }

    private lateinit var headlineText: MaterialTextView
    lateinit var searchView: SearchView
    private lateinit var searchesHistoryList: RecyclerView
    var historyListAdapter: HistoryListAdapter = HistoryListAdapter(context, this)
    private lateinit var settingsButton: MaterialButton
    private lateinit var dataSourceRecyclerView: RecyclerView
    private lateinit var latestSearchesList: RecyclerView
    private lateinit var latestSearchesText: MaterialTextView
    private lateinit var searchProgressBar: SpinKitView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var motionLayout: com.multistream.multistreamsearchview.app_bar.AppBarLayout
    var debounceLength = 500L
    private var searchManager = SearchManager<SearchData>(this)
    private var headlineTextString: String = resources.getString(R.string.search)
    var initializeDefaultFilters: Boolean = false
    private var searchListAdapter = SearchListAdapter()
    private var latestSearchesAdapter = LatestSearchedAdapter()
    private var loadDataJob: Job? = null
    private var quickQueryJob: Job? = null
    private var isSearchClosed = true
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchFocusTransition: Transition
    private var filterExpandTransition =
        TransitionInflater.from(context).inflateTransition(R.transition.filters_expand)
    lateinit var clearButton: MaterialButton
    private lateinit var filterLayout: FilterLayout
    var isFilterLayoutHidden = true
    var hiddenFilterLayoutHeight = 1
    var isDataSourceVisible = true
    private var dataSourceAdapter: DataSourceAdapter? = null
    var selectedButtonColor = R.color.colorOnSurface
    var defaultButtonColor = R.color.colorSurface
    var onAddHistoryData: ((query: String, count: Int) -> Unit)? = null
    var clearLatestDataDialog: AlertDialog? = null
    var onSwipe: ((position: Int) -> Unit)? = null
    private lateinit var searchNoItem: View

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

    init {
        latestSearchesAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onClick(position: Int, view: View) {
                val name = latestSearchesAdapter.currentList[position].name
                searchView.setQuery(name, false)
                postDelayed({
                    TransitionManager.beginDelayedTransition(
                        this@SearchViewLayout,
                        searchFocusTransition
                    )
                    onSearchAnimation()
                }, 2000)
            }
        }
        isFocusableInTouchMode = true
        isFocusable = true
        setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK && !isFilterLayoutHidden) {
                TransitionManager.beginDelayedTransition(this, filterExpandTransition)
                hideFilterLayout()
                return@setOnKeyListener true
            }
            false
        }
    }

    fun addFilter(
        name: String,
        filterSelections: Collection<FilterSelection<SearchData>>,
        isSingleSelection: Boolean = false,
        isAllSelectionEnabled: Boolean = true,
        allName: String?
    ) {
        filterLayout.addFilter(
            name,
            filterSelections,
            isSingleSelection,
            isAllSelectionEnabled,
            allName
        )
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
            onQueryCanceled("debounce")
            loadDataJob = CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) {
                    onQueryStart()
                }
                delay(debounceLength)
                filterLayout.query(query)
            }
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {

        if (searchView.width > 0) {
            onType(newText)
            hideFilterLayout()
            if (!newText.isNullOrBlank()) {
                loadDataJob?.cancel()
                quickQueryJob?.cancel()
                quickQueryJob = CoroutineScope(Dispatchers.Default).launch {
                    withContext(Dispatchers.Main) { onQueryStart() }
                    delay(debounceLength)
                    searchManager.queryData(newText, false)
                }
            }
            return true
        }
        return false
    }


    open class SearchData(
        var id: Int? = null,
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
            setPadding(0, 0, 0, 100)
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
        addView(filterLayout)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
        appBarLayout = coordinatorLayout.findViewById(R.id.app_bar_layout)
        searchView = appBarLayout.findViewById<SearchView>(R.id.search_view).apply {
            isActivated = true
            isEnabled = true
            this.requestFocus()
            this.setOnQueryTextListener(this@SearchViewLayout)
            setOnSearchClickListener {
                onSearch()
            }
            setOnCloseListener {
                onClose()
                searchView.clearFocus()
                searchView.isEnabled = false
                searchView.isActivated = false
                false
            }
        }
        searchNoItem = findViewById(R.id.search_no_item)
        searchesHistoryList = findViewById<RecyclerView>(R.id.history_list).apply {
            this.adapter = historyListAdapter
        }
        settingsButton = findViewById<MaterialButton>(R.id.settings_button).apply {
            elevation = 0f
            this.setOnClickListener {
                it.isSelected = !it.isSelected
                filterAnimation()
            }
        }
        latestSearchesList = findViewById<RecyclerView>(R.id.searchedList).apply {
            adapter = latestSearchesAdapter
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = true

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    onSwipe?.invoke(viewHolder.adapterPosition)
                }
            }).attachToRecyclerView(this)
        }
        dataSourceRecyclerView = findViewById<RecyclerView>(R.id.data_source_list).apply {
        }
        headlineText = appBarLayout.findViewById(R.id.headline)
        val barLayout = appBarLayout.findViewById<ConstraintLayout>(R.id.bar_layout)
        clearButton = barLayout.findViewById<MaterialButton>(R.id.clear_data_button)
        searchRecyclerView = findViewById<RecyclerView>(R.id.search_list).apply {
            adapter = searchListAdapter
        }
        searchProgressBar = findViewById(R.id.progress_bar)
        motionLayout = appBarLayout.findViewById(R.id.motionLayout)
        dataSourceRecyclerView = motionLayout.findViewById(R.id.data_source_list)
        motionLayout.materialTextView = appBarLayout.findViewById(R.id.latest_search_text)
        searchFocusTransition = android.transition.AutoTransition().setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong()).removeTarget(dataSourceRecyclerView).excludeChildren(dataSourceRecyclerView, true)
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

    fun setOnSearchListItemClickListener(onClick: (position: Int, view: View) -> Unit) {
        searchListAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onClick(position: Int, view: View) {
                searchView.onActionViewCollapsed()
                onCloseAnimation()
                onClick(position, view)
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
        onSearchAnimation()
        isSearchClosed = false
        hideFilterLayout()
    }

    private fun onClose() {
        quickQueryJob?.cancel()
        searchProgressBar.visibility = View.INVISIBLE
        searchNoItem.visibility = View.INVISIBLE
        TransitionManager.beginDelayedTransition(this, android.transition.AutoTransition())
        isSearchClosed = true
        onCloseAnimation()
    }

    private fun onSearchAnimation() {
        motionLayout.getConstraintSet(R.id.start).apply {
            getConstraint(R.id.search_view).layout.apply {
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                topMargin = 0
            }
            getConstraint(R.id.settings_button).layout.apply {
                endToEnd = PARENT_ID
            }
            searchView.background =
                ResourcesCompat.getDrawable(resources, R.color.colorSurface, null)
            settingsButton.background =
                ResourcesCompat.getDrawable(resources, R.color.colorSurface, null)
        }
        searchesHistoryList.apply {
            visibility = View.VISIBLE
            scrollToPosition(min(0, historyListAdapter.itemCount - 1))
        }
    }

    private fun onCloseAnimation() {
        searchesHistoryList.visibility = View.GONE
        motionLayout.getConstraintSet(R.id.start).apply {
            getConstraint(R.id.search_view).layout.apply {
                topToTop = UNSET
                topToBottom = R.id.headline
                endToStart = R.id.settings_button
                startToStart = R.id.headline
                topMargin = convertDpToPixel(50, resources)
            }
            getConstraint(R.id.settings_button).layout.apply {
                endToEnd = R.id.headline
            }
        }
        searchView.background =
            ResourcesCompat.getDrawable(resources, R.drawable.rounded_search_view, null)
        settingsButton.background = null
    }

    private fun onType(text: String?) {
        if (text == null) {
            searchesHistoryList.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.GONE
            return
        }
        if (text.isBlank()) {
            quickQueryJob?.cancel()
            loadDataJob?.cancel()
            onQueryCanceled("debounce")
            searchNoItem.visibility = View.INVISIBLE
            searchesHistoryList.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.GONE
        } else {
            searchesHistoryList.visibility = View.INVISIBLE
            searchRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onClick(position: Int, view: View) {
        searchView.setQuery(historyListAdapter.currentList[position]?.searchText, true)
    }

    fun submitHistoryData(list: MutableList<HistoryListAdapter.SearchHistoryData>) {
        historyListAdapter.submitList(list)
    }

    @Suppress("UNCHECKED_CAST")
    fun initSearchView() {
        filterLayout.createFilters()
        initDataSource(searchManager.dataSource.sourceDownloads as MutableList<DataSource.SourceDownloader<SearchData>>)
    }

    private fun filterAnimation() {
        TransitionManager.beginDelayedTransition(this, filterExpandTransition)
        if (isFilterLayoutHidden) {
            showFilterLayout()
        } else {
            hideFilterLayout()
        }
    }

    private fun showFilterLayout() {
        requestFocus()
        filterLayout.layoutParams = (filterLayout.layoutParams as LayoutParams).apply {
            topToBottom = searchView.id
            verticalBias = 1f
            bottomToBottom = PARENT_ID
            isFilterLayoutHidden = false
        }
        isSelected = true
    }

    private fun hideFilterLayout() {
        filterLayout.layoutParams = (filterLayout.layoutParams as LayoutParams).apply {
            topToBottom = PARENT_ID
            bottomToBottom = UNSET
            isFilterLayoutHidden = true
        }
        settingsButton.isSelected = false
    }

    inline fun setOnRecentSearchCancelClickListener(crossinline onClick: (position: Int, view: View) -> Unit) {
        historyListAdapter.onButtonClickListener = object : OnItemClickListener {
            override fun onClick(position: Int, view: View) {
                onClick(position, view)
            }
        }
    }

    inline fun setOnClearButtonClickListener(crossinline onClick: (view: View?) -> Unit) {
        clearLatestDataDialog = MaterialAlertDialogBuilder(context)
            .setPositiveButton(
                resources.getString(android.R.string.ok)
            ) { dialog, which -> onClick(null) }
            .setTitle(R.string.clear_data_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        clearButton.setOnClickListener { clearLatestDataDialog?.show() }
    }

    fun submitLatestSearchList(list: MutableList<LatestSearchedAdapter.LatestSearchData>) {
        clearButton.isEnabled = list.count() > 0
        latestSearchesAdapter.submitList(list)
    }

    fun addLatestSearchesOnItemClickListener(onItemClickListener: OnItemClickListener) {
        latestSearchesAdapter.onItemClickListener = onItemClickListener
    }

    fun getQuery(): String {
        return searchView.query.toString()
    }

    fun getSearchAdapterItem(position: Int): SearchData? {
        return searchListAdapter.searchData?.get(position)
    }

    override fun onQueryStart() {
        searchProgressBar.visibility = View.VISIBLE
        searchListAdapter.searchData = null
    }

    override fun onDataLoad() {
        Log.i("query", "data loading")
    }

    override fun onDataLoaded() {
        Log.i("query", "data loaded")
    }

    override fun onQueryFilter() {
        Log.i("query", "filtering")
    }

    override fun onQueryCanceled(message: String) {
        searchProgressBar.visibility = View.INVISIBLE
    }

    override fun onQueryCompleted(query: String?, isQuickSearch: Boolean, count: Int) {
        if (!isQuickSearch && !query.isNullOrBlank()) onAddHistoryData?.invoke(query, count)
        searchProgressBar.visibility = View.INVISIBLE
        searchNoItem.visibility =
            if (count == 0 && !isSearchClosed && searchView.isInEditMode) View.VISIBLE else View.INVISIBLE
    }

    private fun syncDataSourceData(position: Int, isSelected: Boolean) {
        dataSourceAdapter?.dataSource?.get(position)?.isEnabled = isSelected
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        loadDataJob?.cancel()
        quickQueryJob?.cancel()
    }

    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }
}

interface OnItemClickListener {
    fun onClick(position: Int, view: View)
}

fun convertDpToPixel(dp: Int, resources: Resources): Int {
    return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}