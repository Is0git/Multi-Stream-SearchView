package com.multistream.multistreamsearchview.data_source

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.OnItemClickListener
import com.multistream.multistreamsearchview.search_view.SearchViewLayout

class DataSourceAdapter(var dataSource: MutableList<DataSource.SourceDownloader<SearchViewLayout.SearchData>>)  : RecyclerView.Adapter<DataSourceAdapter.DataSourceViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataSourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.data_source_item, parent, false)
        return DataSourceViewHolder(view).also { it.onItemClickListener = this.onItemClickListener }
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
    class DataSourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var sourceText: MaterialTextView = view.findViewById(R.id.sourceText)

        var onItemClickListener: OnItemClickListener? = null

        init {
            view.setOnClickListener { onItemClickListener?.onClick(adapterPosition, sourceButton) }
        }

        var sourceButton: MaterialCardView = view.findViewById(R.id.sourceButton)

        var icon: ImageView = view.findViewById(R.id.sourceIcon)
//        var button: MaterialButton = view.findViewById(R.id.sourceButton)
    }

}

