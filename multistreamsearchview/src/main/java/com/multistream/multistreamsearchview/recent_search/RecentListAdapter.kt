package com.multistream.multistreamsearchview.recent_search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.multistream.multistreamsearchview.OnItemClickListener
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.SearchViewLayout

class RecentListAdapter(var recentData: List<RecentData>? = null, var clickListener: OnItemClickListener? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var titleText: TextView = view.findViewById(R.id.searchText)

        var searchedCount: TextView = view.findViewById(R.id.searchedCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_item, parent, false)

        return MyViewHolder(view).apply {
            itemView.setOnClickListener { clickListener?.onClick(adapterPosition)}
        }
    }

    override fun getItemCount(): Int {
       return recentData?.count() ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).apply {
            titleText.text = recentData?.get(position)?.searchText
            searchedCount.text = recentData?.get(position)?.searchesCount.toString()
        }
    }

    data class RecentData(var id: Int, var searchText: String? = null, var time: Long, var searchesCount: Int)
}