package com.example.codeblockmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class SignupLoginActivity extends AppCompatActivity {

    private TextView instructionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        instructionView = findViewById(R.id.instruction);
        setSignupView();
    }

    private void setSignupView() {
        String signupText = getResources().getString(R.string.signup);
        String signupKeyword = getResources().getString(R.string.signup_keyword);
        Spannable spannable = new SpannableString(signupText);
        int startIndex = signupText.indexOf(signupKeyword);
        int endIndex = startIndex + signupKeyword.length();

        if (startIndex >= 0) {
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.keyword)),
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        instructionView.setText(spannable);
    }
}