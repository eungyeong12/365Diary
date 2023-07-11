package com.diary.a365diary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity {

    private RelativeLayout loaderLayout;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.logout).setOnClickListener(onClickListener);
        findViewById(R.id.secession).setOnClickListener(onClickListener);
        findViewById(R.id.lockSetting).setOnClickListener(onClickListener);
        loaderLayout = findViewById(R.id.loaderLayout);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();

            if (viewId == R.id.logout) {
                loaderLayout.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().signOut();
                loaderLayout.setVisibility(View.GONE);
                Intent intent = new Intent(SettingActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else if (viewId == R.id.secession) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("정말 탈퇴하시겠습니까?")
                        .setPositiveButton("예", (dialog, which) -> {
                            loaderLayout.setVisibility(View.VISIBLE);
                            user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db.collection("users").document(user.getUid());

                                userRef.delete().addOnSuccessListener(aVoid -> {
                                    // 데이터베이스에서 사용자 정보 삭제 성공
                                    user.delete().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // 사용자 삭제 성공
                                            loaderLayout.setVisibility(View.GONE);
                                            Intent intent1 = new Intent(SettingActivity.this, SignUpActivity.class);
                                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent1);
                                        } else {
                                            // 사용자 삭제 실패
                                            loaderLayout.setVisibility(View.GONE);
                                            // 실패 처리에 대한 로직을 추가하세요.
                                        }
                                    });
                                    });
                                }
                            })
                        .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (viewId == R.id.lockSetting) {
                Intent intent = new Intent(SettingActivity.this, LockSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };

}
