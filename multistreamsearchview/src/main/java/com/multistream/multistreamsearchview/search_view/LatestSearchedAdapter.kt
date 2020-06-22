package com.multistream.multistreamsearchview.search_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.R

class LatestSearchedAdapter(
    var onItemClickListener: OnItemClickListener? = null
) : ListAdapter<LatestSearchedAdapter.LatestSearchData, LatestSearchedAdapter.LatestViewHolder>(diffUtil) {

    class LatestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val searchedName = view.findViewById<MaterialTextView>(R.id.searchedName)
        val category = view.findViewById<MaterialTextView>(R.id.category)
        val date = view.findViewById<MaterialTextView>(R.id.date)
        val platformImage = view.findViewById<ImageView>(R.id.platform_image)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LatestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.latest_searched_item, parent, false)
        return LatestViewHolder(view).also { viewHolder ->
            viewHolder.imageView.setOnClickListener {
                onItemClickListener?.onClick(
                    viewHolder.adapterPosition,
                    it
                )
            }
        }
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        val item = getItem(position)
        Glide.with(holder.itemView.context).load(item?.imageUrl).into(holder.imageView)
        holder.apply {
            searchedName.text = item?.name
            category.text = item?.categoryName
            date.text = item?.dateString
            Glide.with(holder.itemView.context).load(item?.iconId).into(holder.platformImage)
        }
    }

    @Entity(tableName = "latest_searches_table")
    data class LatestSearchData(
        @PrimaryKey(autoGenerate = true) val sql_id: Int = 0, val id: Int = 0, var name: String?,
        var category: Int?,
        var categoryName: String?,
        var dateString: String?,
        var platform: Int?,
        var iconId: Int?,
        var time: Long?,
        var imageUrl: String?
    )
}

val diffUtil = object : DiffUtil.ItemCallback<LatestSearchedAdapter.LatestSearchData>() {
    override fun areItemsTheSame(
        oldItem: LatestSearchedAdapter.LatestSearchData,
        newItem: LatestSearchedAdapter.LatestSearchData
    ): Boolean {
       return oldItem.sql_id == newItem.sql_id
    }

    override fun areContentsTheSame(
        oldItem: LatestSearchedAdapter.LatestSearchData,
        newItem: LatestSearchedAdapter.LatestSearchData
    ): Boolean {
       return oldItem == newItem
    }

}

