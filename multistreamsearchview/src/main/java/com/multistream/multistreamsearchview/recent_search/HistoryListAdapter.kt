package com.multistream.multistreamsearchview.recent_search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.OnItemClickListener
import com.multistream.multistreamsearchview.util.TimeResolver

class HistoryListAdapter(
    var context: Context?,
    var clickListener: OnItemClickListener? = null,
    var onButtonClickListener: OnItemClickListener? = null
) : ListAdapter<HistoryListAdapter.SearchHistoryData, RecyclerView.ViewHolder>(callback) {


    private val DATA_VIEWHOLDER = 0

    private val DATE_VIEWHOLDER = 1

    enum class Time {
        JUST_NOW, MINUTE_AGO, MINUTES_AGO, HOUR_AGO, HOURS_AGO, YESTERDAY, WEEK_AGO, WEEKS_AGO, LONG_TIME_AGO
    }

    var availableTimes = mutableMapOf<Time, @androidx.annotation.IdRes Int>()

    init {
        context?.also {
            availableTimes.apply {
                put(Time.JUST_NOW, R.string.just_now)
                put(Time.MINUTE_AGO, R.string.minute_ago)
                put(Time.MINUTES_AGO, R.string.minutes_ago)
                put(Time.HOUR_AGO, R.string.hour_ago)
                put(Time.HOURS_AGO, R.string.hours_ago)
                put(Time.YESTERDAY, R.string.yesterday)
                put(Time.WEEK_AGO, R.string.less_week_ago)
                put(Time.WEEKS_AGO, R.string.more_week_ago)
                put(Time.LONG_TIME_AGO, R.string.long_time_ago)
            }
        }
    }

    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleText: TextView = view.findViewById(R.id.searchText)
        var searchedCount: TextView = view.findViewById(R.id.searchedCount)
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dateText: TextView = view.findViewById(R.id.dateItem)
        var titleText: TextView = view.findViewById(R.id.searchText)
        var searchedCount: TextView = view.findViewById(R.id.searchedCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = when (viewType) {
            DATA_VIEWHOLDER -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.recent_item, parent, false)
                DataViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.date_item, parent, false)
                DateViewHolder(view)
            }
        }

        return viewHolder.apply {
            itemView.setOnClickListener { clickListener?.onClick(adapterPosition, itemView) }
            itemView.findViewById<View>(R.id.cancel_button)
                .setOnClickListener { onButtonClickListener?.onClick(adapterPosition, itemView) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is DataViewHolder -> {
                holder.titleText.text = item.searchText
                holder.searchedCount.text = holder.searchedCount.context.getString(
                    R.string.results,
                    item?.searchesCount
                )
            }
            is DateViewHolder -> {
                holder.dateText.text = item.dateText
                holder.titleText.text = item.searchText
                holder.searchedCount.text = holder.searchedCount.context.getString(
                    R.string.results,
                    item?.searchesCount
                )
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.showDate!!) {
            DATE_VIEWHOLDER
        } else DATA_VIEWHOLDER
    }

    override fun submitList(list: MutableList<SearchHistoryData>?) {
        groupData(list)
        super.submitList(list)
    }

    private fun groupData(list: List<SearchHistoryData>?) {
        var lastTime: Time? = null
        list?.forEach {
            val time = TimeResolver.getTimeAgo(it.time)
            it.showDate = time != lastTime
            lastTime = time
            it.dateText = context?.getString(availableTimes[lastTime] ?: R.string.default_time)!!
        }
    }

    @Entity(tableName = "search_history_table")
    data class SearchHistoryData constructor(
        @PrimaryKey(autoGenerate = true) var id: Int,
        var searchText: String? = null,
        var time: Long,
        var searchesCount: Int,
        var dateText: String? = null,
        var dateTimeText: String? = null

    ) {
        @Ignore
        var showDate: Boolean = false
    }

}

val callback = object : DiffUtil.ItemCallback<HistoryListAdapter.SearchHistoryData>() {
    override fun areItemsTheSame(
        oldItem: HistoryListAdapter.SearchHistoryData,
        newItem: HistoryListAdapter.SearchHistoryData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: HistoryListAdapter.SearchHistoryData,
        newItem: HistoryListAdapter.SearchHistoryData
    ): Boolean {
        return oldItem == newItem
    }

}