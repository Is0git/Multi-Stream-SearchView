package com.multistream.multistreamsearchview.recent_search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.multistream.multistreamsearchview.search_view.OnItemClickListener
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.util.TimeResolver

class RecentListAdapter(var context: Context?,
    var recentData: List<RecentData>? = null,
    var clickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



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

        groupData()
    }
    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var titleText: TextView = view.findViewById(R.id.searchText)

        var searchedCount: TextView = view.findViewById(R.id.searchedCount)
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dateText: TextView = view.findViewById(R.id.dateItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = when (viewType) {
            DATA_VIEWHOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_item, parent, false)
                DataViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.date_item, parent, false)
                DateViewHolder(view)
            }
        }

        return viewHolder.apply {
            itemView.setOnClickListener { clickListener?.onClick(adapterPosition) }
        }
    }

    override fun getItemCount(): Int {
        return recentData?.count() ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                holder.titleText.text = recentData?.get(position)?.searchText
                holder.searchedCount.text = recentData?.get(position)?.searchesCount.toString()
            }
            is DateViewHolder -> holder.dateText.text = recentData?.get(position)?.dateText
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (recentData?.get(position)?.showDate!!) {
            DATE_VIEWHOLDER
        } else DATA_VIEWHOLDER
    }

    private fun groupData() {
        var lastTime: Time? = null
        recentData?.forEach {
            val time = TimeResolver.getTimeAgo(it.time)
            it.showDate = time != lastTime
            lastTime = time
            it.dateText = context?.getString(availableTimes[lastTime] ?: R.string.default_time)!!
        }
    }

    data class RecentData(
        var id: Int,
        var searchText: String? = null,
        var time: Long,
        var searchesCount: Int,
        var dateText: String? = null,
        var showDate: Boolean = false

    )
}