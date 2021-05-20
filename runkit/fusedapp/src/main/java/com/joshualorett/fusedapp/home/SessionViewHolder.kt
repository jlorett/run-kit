package com.joshualorett.fusedapp.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joshualorett.fusedapp.R
import com.joshualorett.runkit.session.Session

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