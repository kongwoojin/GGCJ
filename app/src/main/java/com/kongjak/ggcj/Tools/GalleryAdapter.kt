package com.kongjak.ggcj.Tools

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kongjak.ggcj.Activity.GalleryReadActivity
import com.kongjak.ggcj.R
import java.util.*

class GalleryAdapter(private val GalleryArrayList: ArrayList<Gallery>) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = GalleryArrayList[position]
        holder.title.text = data.title
        Glide.with(holder.mCardView)
                .load(data.imageUrl)
                .placeholder(R.drawable.ic_thumbnail)
                .centerCrop()
                .apply(RequestOptions().override(200, 200))
                .into(holder.thumbnail)

        holder.mCardView.setOnClickListener {
            val intent = Intent(it.context, GalleryReadActivity::class.java)
            intent.putExtra("url", data.url)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return GalleryArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCardView = itemView.findViewById<CardView>(R.id.card_view)
        val thumbnail = itemView.findViewById<ImageView>(R.id.item_thumbnail)
        val title = itemView.findViewById<TextView>(R.id.item_title)
    }


}