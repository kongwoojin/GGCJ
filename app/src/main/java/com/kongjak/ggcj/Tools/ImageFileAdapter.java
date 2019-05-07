package com.kongjak.ggcj.Tools;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kongjak.ggcj.R;

import java.util.ArrayList;

public class ImageFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ImageFiles> ImageFileArrayList;

    public ImageFileAdapter(ArrayList<ImageFiles> ImageFileArrayList) {
        this.ImageFileArrayList = ImageFileArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_file_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        final ImageFiles data = ImageFileArrayList.get(position);

        if (data.getImageAvailable()) {
            myViewHolder.file.setVisibility(View.GONE);
            Glide.with(myViewHolder.mCardView)
                    .load(data.getUrl())
                    .placeholder(R.drawable.ic_thumbnail)
                    .apply(new RequestOptions().override(1000, 1000))
                    .into(myViewHolder.image);
        } else {
            myViewHolder.image.setVisibility(View.GONE);
            myViewHolder.file.setText(ImageFileArrayList.get(position).title);
        }

        myViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return ImageFileArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        ImageView image;
        TextView file;

        MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.item_image);
            file = view.findViewById(R.id.item_dl);
            mCardView = (CardView) view.findViewById(R.id.dl_card);
        }
    }
}