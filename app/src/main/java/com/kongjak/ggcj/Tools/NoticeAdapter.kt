package com.kongjak.ggcj.Tools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kongjak.ggcj.R
import java.util.*

class NoticeAdapter(
    private val NoticeArrayList: ArrayList<Notices>,
    val itemClick: (Notices) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.notice_item, parent, false)
        return ViewHolder(rootView, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = NoticeArrayList[position]
        var title = if (data.isImportant) {
            holder.itemView.resources.getString(R.string.important_notice) + data.title
        } else {
            data.title
        }
        holder.title.text = title
        holder.writer.text = data.writer
        holder.date.text = data.date
        holder.rootView.setOnClickListener {
            itemClick(data)
        }
    }

    override fun getItemCount(): Int {
        return NoticeArrayList.size
    }

    inner class ViewHolder(itemView: View, itemClick: (Notices) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val rootView = itemView.findViewById<RelativeLayout>(R.id.rootView)
        val title = itemView.findViewById<TextView>(R.id.item_title)
        val writer = itemView.findViewById<TextView>(R.id.item_writer)
        val date = itemView.findViewById<TextView>(R.id.item_date)
    }
}