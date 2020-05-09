package com.multistream.multistreamsearchview

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

class RecentAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, true) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
       val view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false)
        return view
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
       view?.findViewById<TextView>(R.id.categoryText)?.text = cursor?.getString(cursor.getColumnIndexOrThrow("Name"))
    }
}