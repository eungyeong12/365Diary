package com.diary.a365diary;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private PostAdapter adapter;
    private ArrayList<WriteInfo> arrayList;

    private Boolean isExpanded = false;

    private EditText searchEditText;

    private ImageView searchIcon;

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    RelativeLayout mMainLayout;
    RelativeLayout initLayout;
    LinearLayout lockLayout;

    // 비밀번호
    private EditText view_01, view_02, view_03, view_04;

    private final ArrayList<String> numbers_list = new ArrayList<>();
    private String passCode = "";
    private String num_01;
    private String num_02;
    private String num_03;

    private static boolean lock = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // 회원가입 or 로그인
            DocumentReference userRef = db.collection("users").document(user.getUid());

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("userId", user.getUid());
            userRef.set(userData);

            mMainLayout = findViewById(R.id.main_layout);
            lockLayout = findViewById(R.id.lockLayout);

            mMainLayout.setVisibility(View.VISIBLE);
            lockLayout.setVisibility(View.GONE);


        }

        initLayout = findViewById(R.id.initLayout);

        if(lock) {
            lock();
            lock = false;
        }

        searchEditText = findViewById(R.id.searchEditText);
        searchIcon = findViewById(R.id.searchIcon);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        arrayList = new ArrayList<>();
        adapter = new PostAdapter(arrayList, MainActivity.this);
        recyclerView.setAdapter(adapter);

        initLayout.setVisibility(View.VISIBLE);

        TextView init = findViewById(R.id.init);
        init.setOnClickListener(onClickListener);

        EventChangeListener();

        findViewById(R.id.floatingActionButton).setOnClickListener(onClickListener);
        findViewById(R.id.setting).setOnClickListener(onClickListener);
        findViewById(R.id.searchIcon).setOnClickListener(onClickListener);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean getSwitch1State() {
        SharedPreferences preferences = getSharedPreferences("switch1-pref", Context.MODE_PRIVATE);
        String switch1Key = "switch1_state_" + user.getUid();
        return preferences.getBoolean(switch1Key, false);
    }

    private boolean getSwitch2State() {
        SharedPreferences preferences = getSharedPreferences("switch2-pref", Context.MODE_PRIVATE);
        String switch2Key = "switch2_state_" + user.getUid();
        return preferences.getBoolean(switch2Key, false);
    }

    private void lock() {
        if(user != null) {
            if(getSwitch1State()) {
                mMainLayout.setVisibility(View.GONE);
                lockLayout.setVisibility(View.VISIBLE);
                // 비밀번호
                initializeComponents();
            }

            if(getSwitch2State()) {
                // 지문 인증
                BiometricManager biometricManager = BiometricManager.from(this);
                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Toast.makeText(getApplicationContext(), "이 기기는 지문 인식을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "현재 지문 인식을 사용할 수 없습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Toast.makeText(getApplicationContext(), "기기에 등록된 지문이 없습니다.", Toast.LENGTH_SHORT).show();
                }

                Executor executor = ContextCompat.getMainExecutor(this);
                biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        mMainLayout.setVisibility(View.VISIBLE);
                        lockLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("지문 인증")
                        .setNegativeButtonText("비밀번호 사용")
                        .setDeviceCredentialAllowed(false)
                        .build();

                biometricPrompt.authenticate(promptInfo);
            }
        }

    }

    public void initializeComponents() {
        view_01 = findViewById(R.id.view_01);
        view_02 = findViewById(R.id.view_02);
        view_03 = findViewById(R.id.view_03);
        view_04 = findViewById(R.id.view_04);

        Button btn_01 = findViewById(R.id.btn_01);
        Button btn_02 = findViewById(R.id.btn_02);
        Button btn_03 = findViewById(R.id.btn_03);
        Button btn_04 = findViewById(R.id.btn_04);
        Button btn_05 = findViewById(R.id.btn_05);
        Button btn_06 = findViewById(R.id.btn_06);
        Button btn_07 = findViewById(R.id.btn_07);
        Button btn_08 = findViewById(R.id.btn_08);
        Button btn_09 = findViewById(R.id.btn_09);
        Button btn_00 = findViewById(R.id.btn_00);
        ImageView btn_clear = findViewById(R.id.btn_clear);

        btn_01.setOnClickListener(this);
        btn_02.setOnClickListener(this);
        btn_03.setOnClickListener(this);
        btn_04.setOnClickListener(this);
        btn_05.setOnClickListener(this);
        btn_06.setOnClickListener(this);
        btn_07.setOnClickListener(this);
        btn_08.setOnClickListener(this);
        btn_09.setOnClickListener(this);
        btn_00.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_01) {
            numbers_list.add("1");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_02) {
            numbers_list.add("2");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_03) {
            numbers_list.add("3");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_04) {
            numbers_list.add("4");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_05) {
            numbers_list.add("5");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_06) {
            numbers_list.add("6");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_07) {
            numbers_list.add("7");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_08) {
            numbers_list.add("8");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_09) {
            numbers_list.add("9");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_00) {
            numbers_list.add("0");
            passNumber(numbers_list);
        } else if (view.getId() == R.id.btn_clear) {
            if(numbers_list.size() == 1) {
                view_01.setText("");
                numbers_list.clear();
            } else if (numbers_list.size() == 2) {
                view_02.setText("");
                numbers_list.remove(1);
            } else if (numbers_list.size() == 3) {
                view_03.setText("");
                numbers_list.remove(2);
            } else if (numbers_list.size() == 4) {
                view_04.setText("");
                numbers_list.remove(3);
            }
            passNumber(numbers_list);
        }

    }

    private void passNumber(ArrayList<String> numbers_list) {
        if(numbers_list.size() != 0) {
            if(numbers_list.size() == 1) {
                num_01 = numbers_list.get(0);
                view_01.setText("*");
            } else if (numbers_list.size() == 2) {
                num_02 = numbers_list.get(1);
                view_02.setText("*");
            } else if (numbers_list.size() == 3) {
                num_03 = numbers_list.get(2);
                view_03.setText("*");
            } else if (numbers_list.size() == 4) {
                view_04.setText("*");
                String num_04 = numbers_list.get(3);
                passCode = num_01 + num_02 + num_03 + num_04;
                matchPassCode();
            }
        }
    }

    private void matchPassCode() {
        if(getPassCode().equals(passCode)) {
            mMainLayout.setVisibility(View.VISIBLE);
            lockLayout.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            view_01.setText("");
            view_02.setText("");
            view_03.setText("");
            view_04.setText("");
            numbers_list.clear();
        }
    }

    private String getPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode-pref", Context.MODE_PRIVATE);
        return preferences.getString("passcode_" + user.getUid(), "");
    }

    private void filter(String searchText) {
        ArrayList<WriteInfo> filteredList = new ArrayList<>();

        for (WriteInfo writeInfo : arrayList) {
            if (writeInfo.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(writeInfo);
            } else if (writeInfo.getContents().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(writeInfo);
            }
        }

        adapter.filterList(filteredList);
    }


    private void EventChangeListener() {
        if(user == null) return;

        db.collection("posts")
                .whereEqualTo("userId", user.getUid())
                .orderBy("myDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        initLayout.setVisibility(View.GONE);
                        arrayList.clear(); // 기존 데이터 초기화
                        for (DocumentSnapshot snapshot : value) {
                            WriteInfo post = snapshot.toObject(WriteInfo.class);
                            if (post != null) {
                                arrayList.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged(); // 데이터 갱신

                    } else {
                        initLayout.setVisibility(View.GONE);
                    }
                });

    }

    View.OnClickListener onClickListener = view -> {
        if (view.getId() == R.id.floatingActionButton) {
            Intent intent = new Intent(MainActivity.this, WritePostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (view.getId() == R.id.setting) {
            Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent2);
        } else if (view.getId() == R.id.searchIcon) {
            if (!isExpanded) {
                expandSearchBar();
            } else {
                collapseSearchBar();
            }

        } else if (view.getId() == R.id.init) {
            Intent intent = new Intent(MainActivity.this, WritePostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        if (isExpanded) {
            collapseSearchBar();
        } else {
            super.onBackPressed();
        }
    }

    private void expandSearchBar() {

        ViewGroup.LayoutParams layoutParams = searchEditText.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        searchEditText.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams iconLayoutParams = (RelativeLayout.LayoutParams) searchIcon.getLayoutParams();
        iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        searchIcon.setLayoutParams(iconLayoutParams);

        isExpanded = true;
    }

    private void collapseSearchBar() {
        ViewGroup.LayoutParams layoutParams = searchEditText.getLayoutParams();
        layoutParams.width = 0;
        searchEditText.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams iconLayoutParams = (RelativeLayout.LayoutParams) searchIcon.getLayoutParams();
        iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        searchIcon.setLayoutParams(iconLayoutParams);

        searchEditText.setText("");
        isExpanded = false;
    }


}