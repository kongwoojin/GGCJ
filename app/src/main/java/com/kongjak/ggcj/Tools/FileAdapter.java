package com.kongjak.ggcj.Tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kongjak.ggcj.R;

import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    private ArrayList<Files> FileArrayList;

    public FileAdapter(ArrayList<Files> FileArrayList) {
        this.FileArrayList = FileArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_card, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        final Files data = FileArrayList.get(position);

        myViewHolder.title.setText(FileArrayList.get(position).title);

        myViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), data.getUrl(), Toast.LENGTH_SHORT).show();
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return FileArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView title;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.item_dl);
            mCardView = (CardView) view.findViewById(R.id.dl_card);
        }
    }
}