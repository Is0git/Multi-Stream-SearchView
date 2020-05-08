package com.multistream.multistreamsearchview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchListAdapter : RecyclerView.Adapter<SearchListAdapter.MyViewHolder>() {

    var data: List<SearchViewLayout.SearchData>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryText: TextView = itemView.findViewById(R.id.categoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
      return data?.count() ?: 0
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.categoryText.text = data?.get(position)?.platform.toString()
    }
}