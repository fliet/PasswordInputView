package com.example.passwordinputviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PasswordInputView passwordInputView = findViewById(R.id.password_input_view);

        passwordInputView.setOnInputFinishListener(new PasswordInputView.OnInputFinishListener() {
            @Override
            public void onInputFinish(String inputStr) {
                Log.e("PasswordInputView", "inputStr：" + inputStr);
            }
        });
    }
}
