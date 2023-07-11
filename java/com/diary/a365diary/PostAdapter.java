package com.diary.a365diary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<WriteInfo> arrayList;
    private final Context context;

    public PostAdapter(ArrayList<WriteInfo> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {

        WriteInfo writeInfo = arrayList.get(position);

        Glide.with(holder.itemView)
                .load(arrayList.get(position).getImage())
                .override(Target.SIZE_ORIGINAL)
                .into(holder.postImage);

        holder.postTitle.setText(arrayList.get(position).getTitle());
        holder.postContents.setText(arrayList.get(position).getContents());

        // Firebase에서 불러온 문자열 형식의 날짜 데이터

        Date myDate = arrayList.get(position).getMyDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        String date = dateFormat.format(myDate);

        holder.postDate.setText(date);

        holder.itemView.setOnClickListener(view -> {
            int clickedPosition = holder.getBindingAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                WriteInfo writeInfo1 = arrayList.get(clickedPosition);
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("writeInfo", writeInfo1);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public void filterList(ArrayList<WriteInfo> filteredList) {
        arrayList = filteredList;
        notifyDataSetChanged();
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postDate;
        ImageView postImage;
        TextView postTitle;
        TextView postContents;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            this.postDate = itemView.findViewById(R.id.postDate);
            this.postImage = itemView.findViewById(R.id.postImage);
            this.postTitle = itemView.findViewById(R.id.postTitle);
            this.postContents = itemView.findViewById(R.id.postContents);
        }
    }

}