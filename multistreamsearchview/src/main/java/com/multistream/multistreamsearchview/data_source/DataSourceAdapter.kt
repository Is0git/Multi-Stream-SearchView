package com.multistream.multistreamsearchview.data_source

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.OnItemClickListener
import com.multistream.multistreamsearchview.search_view.SearchViewLayout

class DataSourceAdapter(var dataSource: MutableList<DataSource.SourceDownloader<SearchViewLayout.SearchData>>)  : RecyclerView.Adapter<DataSourceAdapter.DataSourceViewHolder>() {

    companion object {
        const val ALL_BUTTON = 0
        const val NORMAL_BUTTON = 1
    }

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataSourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.data_source_item, parent, false)
        return DataSourceViewHolder(view).also {
            it.onItemClickListener = this.onItemClickListener
            val colorId =  if (viewType == ALL_BUTTON) R.color.colorOnSurface else R.color.colorSurface
                val resources = it.sourceButton.context.resources
                val color = ResourcesCompat.getColor(resources, colorId, null)
                val colorStateList = ColorStateList.valueOf(color)
                ViewCompat.setBackgroundTintList(it.sourceButton, colorStateList)
        }
    }

    override fun getItemCount(): Int {
        return dataSource.count()
    }

    override fun onBindViewHolder(holder: DataSourceViewHolder, position: Int) {
        val item = dataSource[position]
        holder.apply {
            sourceText.text = item.name
            icon.background = ResourcesCompat.getDrawable(icon.context.resources, item.iconDrawable, null)
        }
    }

    override fun getItemViewType(position: Int): Int {
       val item = dataSource[position]
        return  if (item.isEnabled) ALL_BUTTON else NORMAL_BUTTON
    }

    class DataSourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var sourceText: MaterialTextView = view.findViewById(R.id.sourceText)
        var onItemClickListener: OnItemClickListener? = null
        var sourceButton: MaterialCardView = view.findViewById(R.id.sourceButton)
        var icon: ImageView = view.findViewById(R.id.sourceIcon)

        init {
            view.setOnClickListener { onItemClickListener?.onClick(adapterPosition, sourceButton) }
        }
    }
}

