package com.diary.a365diary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LockSettingActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private ActivityResultLauncher<Intent> newLockActivityLauncher;

    private SwitchCompat switch1;
    private SwitchCompat switch2;
    private Button fingerprint;
    private Button passwordChange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locksetting);

        newLockActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean switch1Checked = result.getData().getBooleanExtra("switch1_checked", false);
                        switch1.setChecked(switch1Checked);
                        resetPassCode();
                    }
                }
        );

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        fingerprint = findViewById(R.id.fingerprint);
        passwordChange = findViewById(R.id.passwordChange);

        passwordChange.setOnClickListener(onClickListener);


        switch1.setChecked(getSwitch1State());
        if(getSwitch1State()) {
            switch2.setVisibility(View.VISIBLE);
            fingerprint.setEnabled(true);
            passwordChange.setEnabled(true);
            fingerprint.setTextColor(Color.parseColor("#000000"));
            passwordChange.setTextColor(Color.parseColor("#000000"));
        } else {
            switch2.setVisibility(View.GONE);
            fingerprint.setEnabled(false);
            passwordChange.setEnabled(false);
            fingerprint.setTextColor(Color.parseColor("#AEAEAE"));
            passwordChange.setTextColor(Color.parseColor("#AEAEAE"));
            switch2.setChecked(false);
        }

        switch2.setChecked(getSwitch2State());

        switch1.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if(!getSwitch1State()) {
                    startNewLockActivity();
                }
                switch2.setVisibility(View.VISIBLE);
                fingerprint.setEnabled(true);
                passwordChange.setEnabled(true);
                fingerprint.setTextColor(Color.parseColor("#000000"));
                passwordChange.setTextColor(Color.parseColor("#000000"));
            } else {
                switch2.setVisibility(View.GONE);
                fingerprint.setEnabled(false);
                passwordChange.setEnabled(false);
                fingerprint.setTextColor(Color.parseColor("#AEAEAE"));
                passwordChange.setTextColor(Color.parseColor("#AEAEAE"));
                switch2.setChecked(false);
                resetPassCode();
            }
            saveSwitch1State(isChecked);
        });

        switch2.setOnCheckedChangeListener((compoundButton, isChecked) -> saveSwitch2State(isChecked));

    }

    View.OnClickListener onClickListener = view -> {
        if (view.getId() == R.id.passwordChange) {
            boolean isSecond = true;
            setisSecond(isSecond);
            resetPassCode();
            startNewLockActivity();
        }
    };

    private void resetPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("passcode_" + user.getUid());
        editor.apply();
    }

    private void startNewLockActivity() {
        Intent intent = new Intent(LockSettingActivity.this, NewLockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        newLockActivityLauncher.launch(intent);
    }

    private void saveSwitch1State(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences("switch1-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String switch1Key = "switch1_state_" + user.getUid();
        editor.putBoolean(switch1Key, isChecked);
        editor.apply();
    }

    private void saveSwitch2State(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences("switch2-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String switch2Key = "switch2_state_" + user.getUid();
        editor.putBoolean(switch2Key, isChecked);
        editor.apply();
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

    private void setisSecond(boolean isSecond) {
        SharedPreferences preferences = getSharedPreferences("update-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String state = "isSecond_" + user.getUid();
        editor.putBoolean(state, isSecond);
        editor.apply();
    }



}

