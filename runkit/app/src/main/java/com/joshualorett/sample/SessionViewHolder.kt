package com.joshualorett.sample

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joshualorett.runkit.sample.R
import com.joshualorett.runkit.session.Session
import com.joshualorett.sample.database.SessionEntity

/**
 * Created by Joshua on 4/11/2021.
 */
class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var date: TextView = itemView.findViewById(R.id.date)
    private var title: TextView = itemView.findViewById(R.id.title)

    fun bind(item: Session?) {
        item?.let {
            date.text = it.endTime
            title.text = it.title
        }
    }
}