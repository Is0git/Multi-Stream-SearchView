package com.multistream.multistreamsearchview.search_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multistream.multistreamsearchview.R

class LatestSearchedAdapter(var recentSearchedData: List<SearchViewLayout.SearchData>? = null) : RecyclerView.Adapter<LatestSearchedAdapter.LatestViewHolder>() {

    class LatestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestSearchedAdapter.LatestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.latest_searched_item, parent, false)
        return LatestViewHolder(view)
    }

    override fun getItemCount(): Int {
       return  recentSearchedData?.count() ?: 0
    }

    override fun onBindViewHolder(holder: LatestSearchedAdapter.LatestViewHolder, position: Int) {
        val item = recentSearchedData?.get(position)
        Glide.with(holder.itemView.context).load(item?.imageUrl).into(holder.imageView)
    }
}