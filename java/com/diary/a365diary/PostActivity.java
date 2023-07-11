package com.diary.a365diary;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private WriteInfo writeInfo;
    private RelativeLayout loaderLayout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ImageView menuImage = findViewById(R.id.menuImage);
        menuImage.setOnClickListener(this::showPopup);
        loaderLayout = findViewById(R.id.loaderLayout);
        writeInfo = (WriteInfo) getIntent().getSerializableExtra("writeInfo");

        TextView postDate1 = findViewById(R.id.postDate1);
        TextView postDate2 = findViewById(R.id.postDate2);
        ImageView postImage = findViewById(R.id.postImage);
        TextView postTitle = findViewById(R.id.postTitle);
        TextView postContents = findViewById(R.id.postContents);


        Date dateText = writeInfo.getMyDate();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy", Locale.US);
        String date1 = dateFormat1.format(dateText);

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM.dd", Locale.US);
        String date2 = dateFormat2.format(dateText);

        postDate1.setText(date1);
        postDate2.setText(date2);

        String image = writeInfo.getImage();

        // 변환된 문자열을 TextView에 표시
        postTitle.setText(writeInfo.getTitle());
        postContents.setText(writeInfo.getContents());

        Glide.with(this)
                .load(image)
                .into(postImage);

    }


    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(PostActivity.this, v);
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.modify) {
                Intent intent = new Intent(PostActivity.this, WritePostActivity.class);
                intent.putExtra("writeInfo", writeInfo);
                startActivity(intent);
            } else if (menuItem.getItemId() == R.id.delete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setMessage("삭제하시겠습니까?")
                        .setPositiveButton("예", (dialog, which) -> {
                            loaderLayout.setVisibility(View.VISIBLE);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("posts")
                                    .whereEqualTo("createAt", writeInfo.getCreateAt())
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            // 게시물에 연결된 이미지 URL 가져오기

                                            document.getReference().delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        loaderLayout.setVisibility(View.GONE);
                                                        Toast.makeText(PostActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        loaderLayout.setVisibility(View.GONE);
                                                        Toast.makeText(PostActivity.this, "삭제하지 못하였습니다.", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    });
                        }).setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                        .show();

            }
            return false;
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }
}