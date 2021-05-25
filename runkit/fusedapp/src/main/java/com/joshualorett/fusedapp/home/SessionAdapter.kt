package com.joshualorett.fusedapp.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.joshualorett.fusedapp.R
import com.joshualorett.runkit.session.Session

class SessionAdapter: PagingDataAdapter<Session, SessionViewHolder>(sessionComparator) {
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item_session, parent, false)
        return SessionViewHolder(layout)
    }

    companion object {
        val sessionComparator = object : DiffUtil.ItemCallback<Session>() {
            override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
                return oldItem == newItem
            }
        }
    }
}
