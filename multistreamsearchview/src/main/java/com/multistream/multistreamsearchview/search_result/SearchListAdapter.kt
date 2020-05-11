package com.multistream.multistreamsearchview.search_result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.SearchViewLayout
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.CHANNELS
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.GAMES
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.STREAMS

class SearchListAdapter() : RecyclerView.Adapter<SearchListAdapter.SearchViewHolder>() {

    var searchData: List<SearchViewLayout.SearchData>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)

        val text: TextView = view.findViewById(R.id.text)

        val categoryText: TextView = view.findViewById(R.id.category_text)

        var platformImage: ImageView = view.findViewById(R.id.platform_image)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
       val view = when(viewType) {
            GAMES -> layoutInflater.inflate(R.layout.game_item, parent, false)
            CHANNELS -> layoutInflater.inflate(R.layout.channel_item, parent, false)
            else -> layoutInflater.inflate(R.layout.stream_item, parent, false)
        }
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int {
       return searchData?.count() ?: 0
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = searchData?.get(position)
        holder.apply {
            text.text = item?.title
            categoryText.text = text.context.getString(item?.categoryStringId!!)
            Glide.with(image).load(item.imageUrl).centerInside().into(image)
            Glide.with(platformImage).load(item.platformResId).centerInside().into(platformImage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = searchData?.get(position)
        return when(item?.category) {
            GAMES -> GAMES
            CHANNELS -> CHANNELS
            STREAMS -> STREAMS
            else -> CHANNELS
        }
    }
}