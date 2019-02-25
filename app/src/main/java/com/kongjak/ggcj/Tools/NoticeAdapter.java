package com.kongjak.ggcj.Tools;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kongjak.ggcj.Activity.NoticeReadActivity;
import com.kongjak.ggcj.R;

import java.util.ArrayList;

public class NoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Notices> NoticeArrayList;

    public NoticeAdapter(ArrayList<Notices> NoticeArrayList) {
        this.NoticeArrayList = NoticeArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        final Notices data = NoticeArrayList.get(position);

        myViewHolder.title.setText(NoticeArrayList.get(position).title);
        myViewHolder.writer.setText(NoticeArrayList.get(position).writer);
        myViewHolder.date.setText(NoticeArrayList.get(position).date);

        myViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), data.getUrl(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), NoticeReadActivity.class);
                intent.putExtra("url", data.getUrl());
                view.getContext().startActivity(intent);
                //view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return NoticeArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView title;
        TextView writer;
        TextView date;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.item_title);
            writer = view.findViewById(R.id.item_writer);
            date = view.findViewById(R.id.item_date);
            mCardView = (CardView) view.findViewById(R.id.card_view);

        }
    }
}