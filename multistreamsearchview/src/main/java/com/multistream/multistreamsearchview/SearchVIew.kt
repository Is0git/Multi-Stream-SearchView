package com.multistream.multistreamsearchview

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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.data_sync.MultipleSelectionDataSync
import com.multistream.multistreamsearchview.data_sync.SingleSelectionDataSync
import com.multistream.multistreamsearchview.filter.FilterLayout
import com.multistream.multistreamsearchview.recent_search.RecentListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SearchViewLayout : ConstraintLayout, SearchView.OnQueryTextListener, OnItemClickListener {

    lateinit var headlineText: MaterialTextView

    lateinit var searchView: SearchView

    lateinit var recentSearchesList: RecyclerView

    lateinit var recentListAdapter: RecentListAdapter

    var headlineTextString: String = resources.getString(R.string.search)

    var initializeDefaultFilters: Boolean = false

    var searchListAdapter = SearchListAdapter()

    var loadDataJob: Job? = null

    lateinit var filterLayout: FilterLayout

    val multipleSelectionDataSync: MultipleSelectionDataSync<SearchData> by lazy { MultipleSelectionDataSync<SearchData>() }

    lateinit var recyclerView: RecyclerView

    var searchFocusTransition = TransitionInflater.from(context).inflateTransition(R.transition.on_search_focus_transition)

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

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        loadDataJob =
            CoroutineScope(Dispatchers.Default).launch { filterLayout.query(query!!) }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onType(newText)
        return true
    }

    data class SearchData(
        var title: String? = null,
        var imageUrl: String? = null,
        var category: Int? = null,
        var platform: Int
    )


    private fun init(context: Context?, attrs: AttributeSet? = null) {
        filterLayout.getFilteredObserver()?.observe(
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
            typeface = Typeface.DEFAULT_BOLD
            text = headlineTextString
            TextViewCompat.setTextAppearance(
                this,
                R.style.TextAppearance_MaterialComponents_Headline3
            )
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                marginStart = convertDpToPixel(35, resources)
                marginEnd = convertDpToPixel(35, resources)
                topMargin = convertDpToPixel(50, resources)
                endToEnd = PARENT_ID
            }
        }
        searchView = SearchView(context).apply {
            id = R.id.search_view
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
                endToEnd = headlineText.id
                topToBottom = headlineText.id
                startToStart = headlineText.id
                topMargin = convertDpToPixel(50, resources)
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

        val list = listOf(
            RecentListAdapter.RecentData(1, "greekgodx", 145, 5),
            RecentListAdapter.RecentData(1, "yassuo", 145, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 145, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 145, 10),
            RecentListAdapter.RecentData(1, "greekgodx", 145, 5),
            RecentListAdapter.RecentData(1, "yassuo", 145, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 145, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 145, 10),
            RecentListAdapter.RecentData(1, "greekgodx", 145, 5),
            RecentListAdapter.RecentData(1, "yassuo", 145, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 145, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 145, 10),            RecentListAdapter.RecentData(1, "greekgodx", 145, 5),
            RecentListAdapter.RecentData(1, "yassuo", 145, 51),
            RecentListAdapter.RecentData(1, "nadeshot", 145, 12),
            RecentListAdapter.RecentData(1, "drdisresepect", 145, 10)

        )
        recentListAdapter = RecentListAdapter(list, this)

        recentSearchesList = RecyclerView(context).apply {
            id = View.generateViewId()
            setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSurface, null))
            layoutParams = LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT).apply {
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
                bottomToBottom = PARENT_ID
                topToBottom = searchView.id
            }
            visibility = View.GONE
            layoutManager = LinearLayoutManager(context)
            adapter = recentListAdapter
        }
        addView(headlineText)
        addView(searchView)
        addView(recentSearchesList)

        searchView.setOnSearchClickListener {
            onSearch()
        }
        searchView.setOnCloseListener { onClose()
        false}

        addView(recyclerView)
    }

    private fun onSearch(){
        TransitionManager.beginDelayedTransition(this, searchFocusTransition)
        recentSearchesList.visibility = View.VISIBLE
        headlineText.visibility = View.INVISIBLE
        searchView.layoutParams = (searchView.layoutParams as LayoutParams).apply {
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            topToTop = PARENT_ID
            topMargin = 0
        }
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
            topMargin = convertDpToPixel(50, resources)
        }
    }

    private fun onType(text: String?) {
        recentSearchesList.visibility = if (text.isNullOrBlank())  View.VISIBLE else View.INVISIBLE
    }

    override fun onClick(position: Int) {
        searchView.setQuery(recentListAdapter.recentData?.get(position)?.searchText, true)
    }

}

interface OnItemClickListener {
    fun onClick(position: Int)
}

 fun convertDpToPixel(dp: Int, resources: Resources): Int {
    return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}