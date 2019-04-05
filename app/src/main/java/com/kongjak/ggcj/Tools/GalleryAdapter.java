package com.kongjak.ggcj.Tools;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kongjak.ggcj.Activity.GalleryReadActivity;
import com.kongjak.ggcj.Activity.NoticeReadActivity;
import com.kongjak.ggcj.R;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Gallery> GalleryArrayList;

    public GalleryAdapter(ArrayList<Gallery> GalleryArrayList) {
        this.GalleryArrayList = GalleryArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        final Gallery data = GalleryArrayList.get(position);

        myViewHolder.title.setText(GalleryArrayList.get(position).title);
        myViewHolder.thumbnail.setImageDrawable(GalleryArrayList.get(position).thumbnail);

        myViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), data.getUrl(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), GalleryReadActivity.class);
                intent.putExtra("url", data.getUrl());
                view.getContext().startActivity(intent);
                //view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return GalleryArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView title;
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.item_title);
            thumbnail = view.findViewById(R.id.item_thumbnail);
            mCardView = (CardView) view.findViewById(R.id.card_view);

        }
    }
}