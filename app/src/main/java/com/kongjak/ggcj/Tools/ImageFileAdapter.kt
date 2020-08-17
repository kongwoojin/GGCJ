package com.kongjak.ggcj.Tools

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kongjak.ggcj.R
import java.util.*

class ImageFileAdapter(private val ImageFileArrayList: ArrayList<ImageFiles>) : RecyclerView.Adapter<ImageFileAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_file_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = ImageFileArrayList[position]
        if (data.imageAvailable) {
            holder.file.visibility = View.GONE
            Glide.with(holder.mCardView)
                    .load(data.url)
                    .placeholder(R.drawable.ic_thumbnail)
                    .apply(RequestOptions().override(1000, 1000))
                    .into(holder.image)
        } else {
            holder.image.visibility = View.GONE
            holder.file.text = ImageFileArrayList[position].title
        }
        holder.mCardView.setOnClickListener {
            it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.url)))
        }
    }

    override fun getItemCount(): Int {
        return ImageFileArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCardView = itemView.findViewById<CardView>(R.id.dl_card)
        val image = itemView.findViewById<ImageView>(R.id.item_image)
        val file = itemView.findViewById<TextView>(R.id.item_dl)
    }
}