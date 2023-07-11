package com.diary.a365diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WritePostActivity extends AppCompatActivity {
    private FirebaseUser user;
    private Uri uri;
    private ImageView imageView;
    private TextView dateText1;
    private TextView dateText2;

    private RelativeLayout loaderLayout;
    private WriteInfo writeInfo;

    private static boolean isEdit = false;

    private static boolean isImageDelete = true;
    private Date myDate;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        user = FirebaseAuth.getInstance().getCurrentUser();



        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.camera).setOnClickListener(onClickListener1);
        findViewById(R.id.deleteButton).setOnClickListener(onClickListener1);
        loaderLayout = findViewById(R.id.loaderLayout);

        imageView = findViewById(R.id.image_view);
        imageView.setOnClickListener(onClickListener1);

        dateText1 = findViewById(R.id.dateText1);
        dateText2 = findViewById(R.id.dateText2);


        writeInfo = (WriteInfo) getIntent().getSerializableExtra("writeInfo");
        if (writeInfo != null) {
            isEdit = true;
            displayPostData(writeInfo);
        } else {
            dateText1.setText(getTime1());
            dateText2.setText(getTime2());
        }

        dateText1.setOnClickListener(onClickListener1);
        dateText2.setOnClickListener(onClickListener1);

    }



    private String getTime1() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.US);
        return dateFormat.format(date);
    }

    private String getTime2() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd", Locale.US);
        return dateFormat.format(date);
    }


    private void displayPostData(WriteInfo writeInfo) {
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText contentsEditText = findViewById(R.id.contentsEditText);

        titleEditText.setText(writeInfo.getTitle());
        contentsEditText.setText(writeInfo.getContents());

        if(writeInfo.getImage() != null) {
            isImageDelete = false;
            Glide.with(this)
                    .load(writeInfo.getImage())
                    .into(imageView);
            findViewById(R.id.camera).setVisibility(View.GONE);
        }

        Date dateText = writeInfo.getMyDate();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy", Locale.US);
        String date1 = dateFormat1.format(dateText);

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM.dd", Locale.US);
        String date2 = dateFormat2.format(dateText);

        dateText1.setText(date1);
        dateText2.setText(date2);


    }

    View.OnClickListener onClickListener1 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.camera) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityResult.launch(intent);
            } else if (view.getId() == R.id.image_view) {
                if (imageView.getDrawable() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WritePostActivity.this);
                    builder.setMessage("이미지를 삭제하시겠습니까?")
                            .setPositiveButton("예", (dialog, which) -> {
                                isImageDelete = true;
                                imageView.setImageDrawable(null);
                                findViewById(R.id.camera).setVisibility(View.VISIBLE);
                            })
                            .setNegativeButton("아니오", null)
                            .show();
                } else {
                    Intent intent2 = new Intent(Intent.ACTION_PICK);
                    intent2.setType("image/*");
                    startActivityResult.launch(intent2);
                }
            } else if (view.getId() == R.id.deleteButton) {
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(WritePostActivity.this);
                deleteBuilder.setMessage("작성 중인 글을 취소하시겠습니까?")
                        .setPositiveButton("예", (dialog, which) -> finish())
                        .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (view.getId() == R.id.dateText1) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
            } else if (view.getId() == R.id.dateText2) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
            }

        }
    };

    View.OnClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View view) {
            if(view.getId() == R.id.check) {
                if (isEdit) {
                    updatePost();
                } else {
                    storageUpload();
                }
            }
        }
    };

    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        Glide.with(WritePostActivity.this)
                                .load(uri)
                                .into(imageView);
                        findViewById(R.id.camera).setVisibility(View.GONE);
                        isImageDelete = false;
                        findViewById(R.id.camera).setVisibility(View.GONE);
                    }
                }
            }
    );

    private void storageUpload() {
        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        final String contents = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();


        String myDateText = dateText1.getText().toString() + "." + dateText2.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);

        try {
            myDate = dateFormat.parse(myDateText + " " +
                    calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    calendar.get(Calendar.MINUTE) + ":" +
                    calendar.get(Calendar.SECOND));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        if (title.length() > 0 && contents.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            user = FirebaseAuth.getInstance().getCurrentUser();


            if(imageView.getDrawable() != null && !isImageDelete) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child("images/" + uri.getLastPathSegment());
                UploadTask uploadTask = storageRef.putFile(uri);

                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        if (task.getException() != null) {
                            throw task.getException();
                        } else {
                            throw new Exception("Task execution failed");
                        }
                    }
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();



                if (user != null && !isImageDelete) {
                    writeInfo = new WriteInfo(title, contents, user.getUid(), imageUrl, new Date(), myDate);
                    storageUploader(writeInfo);
                }

                Intent intent = new Intent(WritePostActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
                });
            } else {
                if (user != null) {
                    WriteInfo writeInfo = new WriteInfo(title, contents, user.getUid(), null, new Date(), myDate);
                    storageUploader(writeInfo);
                    loaderLayout.setVisibility(View.GONE);
                    Intent intent = new Intent(WritePostActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }


        } else {
            startToast("제목과 내용을 모두 입력해주세요.");
        }
    }

    private void storageUploader(WriteInfo writeInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(writeInfo)
                .addOnSuccessListener(documentReference -> loaderLayout.setVisibility(View.GONE))
                .addOnFailureListener(e -> loaderLayout.setVisibility(View.GONE));

    }

    private void updatePost() {
        isEdit = false;
        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        final String contents = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        String myDateText = dateText1.getText().toString() + "." + dateText2.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(writeInfo.getMyDate());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US);

        try {
            myDate = dateFormat.parse(myDateText + " " +
                    calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    calendar.get(Calendar.MINUTE) + ":" +
                    calendar.get(Calendar.SECOND));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        writeInfo.setMyDate(myDate);

        if (title.length() > 0 && contents.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            user = FirebaseAuth.getInstance().getCurrentUser();

            writeInfo.setTitle(title);
            writeInfo.setContents(contents);


            if (uri != null && !isImageDelete) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child("images/" + uri.getLastPathSegment());
                UploadTask uploadTask = storageRef.putFile(uri);

                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        if (task.getException() != null) {
                            throw task.getException();
                        } else {
                            throw new Exception("Task execution failed");
                        }
                    }
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();
                        writeInfo.setImage(imageUrl);
                        updatePostData();
                    }
                }).addOnFailureListener(e -> loaderLayout.setVisibility(View.GONE));
            } else if (isImageDelete){
                writeInfo.setImage(null);
                updatePostData();
            } else {
                updatePostData();
            }
        } else {
            startToast("제목과 내용을 모두 입력해주세요.");
        }
    }

    private void updatePostData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .whereEqualTo("createAt", writeInfo.getCreateAt()) // Assuming "createAt" is the field name in Firestore
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        document.getReference().set(writeInfo)
                                .addOnSuccessListener(aVoid -> {
                                    loaderLayout.setVisibility(View.GONE);
                                    startToast("수정되었습니다.");
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> loaderLayout.setVisibility(View.GONE));
                    } else {
                        loaderLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> loaderLayout.setVisibility(View.GONE));
    }



    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WritePostActivity.this);
        builder.setMessage("작성 중인 글을 취소하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> finish())
                .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
