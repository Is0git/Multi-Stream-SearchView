package com.multistream.multistreamsearchview

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.core.widget.TextViewCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView


class SearchViewLayout : ConstraintLayout {

    lateinit var titleText: MaterialTextView

    lateinit var searchView: SearchView

    lateinit var searchFilterManager: SearchFilterManager<SearchData>

    var defaultFilterName = resources.getString(R.string.default_filter_name)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun init(context: Context?, attrs: AttributeSet?) {
        createFilterViews()
    }

    data class SearchData(
        var title: String? = null,
        var imageUrl: String? = null,
        var platform: Int
    )

    private fun createFilterViews() {
        searchFilterManager.filters?.forEach { searchDataFilter ->
            val filterHeadline = createFilterHeadline(searchDataFilter.filterName ?: defaultFilterName)
            addView(filterHeadline)
            val chipGroup = createChipGroup(filterHeadline)
            searchDataFilter.filterSelections?.forEach {
                  createChip(it.dataName).also { chipGroup.addView(it) }
            }
            addView(chipGroup)
        }
    }

    private fun createFilterHeadline(title: String): TextView {
        val lastChildView = getChildAt(childCount - 1)

        val newLayoutParams = LayoutParams(convertDpToPixel(100), LayoutParams.WRAP_CONTENT).also {
            it.topToBottom = lastChildView.id
            it.startToStart = searchView.id
        }
        val text = MaterialTextView(context, null, R.attr.textAppearanceBody1).apply {
            id = View.generateViewId()
            text = title
            layoutParams = newLayoutParams
        }
        TextViewCompat.setTextAppearance(text, R.style.TextAppearance_MaterialComponents_Body1)
        return text
    }

    private fun createChipGroup(titleView: View) : ChipGroup {
        return ChipGroup(context).apply {
            layoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).also {
                it.startToEnd = titleView.id
                it.topToTop = titleView.id
                it.endToEnd = searchView.id
            }
        }
    }

    private fun createChip(title: String?) : Chip {
        return Chip(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            text = title
        }
    }

    private fun convertDpToPixel(dp: Int): Int {
        return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}