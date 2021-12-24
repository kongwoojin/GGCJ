package com.kongjak.ggcj.Tools

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kongjak.ggcj.R
import java.util.*

class FileAdapter(private val FileArrayList: ArrayList<Files>) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = FileArrayList[position]
        holder.title.text = data.title
        holder.mCardView.setOnClickListener {
            it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.url)))
        }
    }


    override fun getItemCount(): Int {
        return FileArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCardView = itemView.findViewById<CardView>(R.id.dl_card)
        val title = itemView.findViewById<TextView>(R.id.item_dl)
    }
}