package com.diary.a365diary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class NewLockActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText view_01, view_02, view_03, view_04;

    private final ArrayList<String> numbers_list = new ArrayList<>();
    private String passCode = "";
    private String num_01;
    private String num_02;
    private String num_03;

    private TextView textView;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newlock);
        initializeComponents();
        textView = findViewById(R.id.textView);
        textView.setText("새 비밀번호를 입력해주세요");


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
                if(getPassCode().length() == 0) {
                    savePassCode(passCode);
                } else {
                    matchPassCode();
                }

            }
        }
    }


    private void matchPassCode() {
        if(getPassCode().equals(passCode)) {
            SharedPreferences preferences = getSharedPreferences("isLock-pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isLock", true);
            editor.apply();
            setExistPassCode(passCode);
            finish();
        } else {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            view_01.setText("");
            view_02.setText("");
            view_03.setText("");
            view_04.setText("");
            numbers_list.clear();
        }
    }

    private void savePassCode(String passCode) {
        SharedPreferences preferences = getSharedPreferences("passcode-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("passcode_" + user.getUid(), passCode);
        editor.apply();

        textView.setText("한 번 더 입력해주세요");
        view_01.setText("");
        view_02.setText("");
        view_03.setText("");
        view_04.setText("");
        numbers_list.clear();

    }

    private String getPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode-pref", Context.MODE_PRIVATE);
        return preferences.getString("passcode_" + user.getUid(), "");
    }

    @Override
    public void onBackPressed() {
        if(!getIsSecond()) {
            numbers_list.clear();
            resetPassCode();
            Intent intent = new Intent();
            intent.putExtra("switch1_checked", false);
            setResult(RESULT_OK, intent);
        } else {
            passCode = getExistPassCode();
            savePassCode(passCode);
            boolean isSecond = false;
            setIsSecond(isSecond);

        }
        super.onBackPressed();
    }

    private void resetPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("passcode_" + user.getUid());
        editor.apply();
    }

    private void setIsSecond(boolean isSecond) {
        SharedPreferences preferences = getSharedPreferences("update-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String state = "isSecond_" + user.getUid();
        editor.putBoolean(state, isSecond);
        editor.apply();
    }

    private boolean getIsSecond() {
        SharedPreferences preferences = getSharedPreferences("update-pref", Context.MODE_PRIVATE);
        String state = "isSecond_" + user.getUid();
        return preferences.getBoolean(state, false);
    }

    private void setExistPassCode(String passCode) {
        SharedPreferences preferences = getSharedPreferences("existPasscode-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("passcode_" + user.getUid(), passCode);
        editor.apply();
    }

    private String getExistPassCode() {
        SharedPreferences preferences = getSharedPreferences("existPasscode-pref", Context.MODE_PRIVATE);
        String existPassCode = "passcode_" + user.getUid();
        return preferences.getString(existPassCode, "");
    }

}
