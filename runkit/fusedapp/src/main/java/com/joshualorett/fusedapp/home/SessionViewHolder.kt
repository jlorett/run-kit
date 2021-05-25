package com.joshualorett.fusedapp.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joshualorett.fusedapp.R
import com.joshualorett.fusedapp.formatDistance
import com.joshualorett.fusedapp.time.formatHoursMinutesSeconds
import com.joshualorett.fusedapp.time.formatMinutesSeconds
import com.joshualorett.runkit.session.Session

class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val date: TextView = itemView.findViewById(R.id.date)
    private val title: TextView = itemView.findViewById(R.id.title)
    private val distance: TextView = itemView.findViewById(R.id.distance)
    private val time: TextView = itemView.findViewById(R.id.time)
    private val pace: TextView = itemView.findViewById(R.id.pace)

    fun bind(item: Session?) {
        item?.let {
            date.text = it.endTime
            title.text = it.title
            distance.text = formatDistance(it.distance)
            time.text = formatHoursMinutesSeconds(it.elapsedTime.toDouble())
            pace.text =  "${formatMinutesSeconds(it.averagePace())} /km"
        }
    }
}