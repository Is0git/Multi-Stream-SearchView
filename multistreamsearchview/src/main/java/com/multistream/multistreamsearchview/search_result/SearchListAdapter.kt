package com.multistream.multistreamsearchview.search_result

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.OnItemClickListener
import com.multistream.multistreamsearchview.search_view.SearchViewLayout
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.CHANNELS
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.GAMES
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.STREAMS

class SearchListAdapter : RecyclerView.Adapter<SearchListAdapter.SearchViewHolder>() {

    var searchData: List<SearchViewLayout.SearchData>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClickListener: OnItemClickListener? = null

    sealed class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.image)
        val text: TextView = view.findViewById(R.id.text)
        val categoryText: TextView = view.findViewById(R.id.game_text)
        var platformImage: ImageView = view.findViewById(R.id.platform_image)

        class ChannelsViewHolder(view: View) : SearchViewHolder(view)
        class GamesViewHolder(view: View) : SearchViewHolder(view)
        class StreamsViewHolder(view: View) : SearchViewHolder(view) {

            var liveImage: ImageView = view.findViewById(R.id.live_image)
            var viewerCount: TextView = view.findViewById(R.id.followers_text)
            var liveImageDrawable: AnimatedVectorDrawable? = null

            init {
                liveImageDrawable = if (liveImage.drawable is AnimatedVectorDrawable) liveImage.drawable as AnimatedVectorDrawable else null
            }

            fun playLiveVectorAnimation() {
                liveImageDrawable?.start()
            }

            fun stopLiveVectorAnimation() {
                liveImageDrawable?.stop()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewHolder = when (viewType) {
            GAMES -> {
                val view = layoutInflater.inflate(R.layout.game_item, parent, false)
                SearchViewHolder.GamesViewHolder(view)
            }
            CHANNELS -> {
                val view = layoutInflater.inflate(R.layout.channel_item, parent, false)
                SearchViewHolder.ChannelsViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.stream_item, parent, false)
                SearchViewHolder.StreamsViewHolder(view)
            }
        }
        return viewHolder.also { holder ->
            holder.itemView.setOnClickListener {
                onItemClickListener?.onClick(
                    holder.adapterPosition,
                    it
                )
            }
        }
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
        if (item is StreamSearchData) {
            (holder as SearchViewHolder.StreamsViewHolder).apply {
                this.viewerCount.text = item.viewersCount.toString()
                holder.playLiveVectorAnimation()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = searchData?.get(position)
        return when (item?.category) {
            GAMES -> GAMES
            CHANNELS -> CHANNELS
            STREAMS -> STREAMS
            else -> CHANNELS
        }
    }

    override fun onViewAttachedToWindow(holder: SearchViewHolder) {
        if (holder is SearchViewHolder.StreamsViewHolder) {
            holder.playLiveVectorAnimation()
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: SearchViewHolder) {
        if (holder is SearchViewHolder.StreamsViewHolder) {
            holder.stopLiveVectorAnimation()
        }
        super.onViewDetachedFromWindow(holder)
    }

   open class StreamSearchData(
        id: Int? = null,
        title: String? = null,
        imageUrl: String? = null,
        viewers: Int,
        platform: Int,
        @DrawableRes platformResId: Int
    ) : SearchViewLayout.SearchData(
        id,
        title,
        imageUrl,
        STREAMS,
        R.string.streams_category,
        platform,
        platformResId
    ) {
       var viewersCount = viewers
   }
}