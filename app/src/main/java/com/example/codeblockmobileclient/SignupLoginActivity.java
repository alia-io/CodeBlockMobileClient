package com.example.codeblockmobileclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupLoginActivity extends AppCompatActivity {

    private enum ActivityOption { SIGNUP, LOGIN };
    private ActivityOption currentOption;

    private TextView instructionView;
    private TextView errorView;
    private TextInputLayout confirmPasswordLayout;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button submitButton;

    private TextView toggleView;
    private Button toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        instructionView = findViewById(R.id.instruction);
        errorView = findViewById(R.id.error_text);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        emailInput = findViewById(R.id.email_address);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        submitButton = findViewById(R.id.signup_login_button);
        toggleView = findViewById(R.id.toggle_text);
        toggleButton = findViewById(R.id.toggle_button);
        setToLogin();
    }

    private void setToSignup() {

        String signupText = getResources().getString(R.string.signup);
        String signupKeyword = getResources().getString(R.string.new_keyword);
        Spannable spannable = new SpannableString(signupText);
        int startIndex = signupText.indexOf(signupKeyword);
        int endIndex = startIndex + signupKeyword.length();

        if (startIndex >= 0) {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(),
                    R.color.keyword)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        instructionView.setText(spannable);
        errorView.setText("");
        errorView.setVisibility(View.GONE);
        confirmPasswordLayout.setVisibility(View.VISIBLE);
        submitButton.setText(getResources().getString(R.string.signup_button));
        toggleView.setText(getResources().getString(R.string.switch_to_login));
        toggleButton.setText(getResources().getString(R.string.switch_to_login_button));
        currentOption = ActivityOption.SIGNUP;
    }

    private void setToLogin() {

        String signupText = getResources().getString(R.string.switch_to_signup);
        String signupKeyword = getResources().getString(R.string.new_keyword);
        Spannable spannable = new SpannableString(signupText);
        int startIndex = signupText.indexOf(signupKeyword);
        int endIndex = startIndex + signupKeyword.length();

        if (startIndex >= 0) {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(),
                    R.color.keyword)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        instructionView.setText(getResources().getString(R.string.login));
        errorView.setText("");
        errorView.setVisibility(View.GONE);
        confirmPasswordInput.setText("");
        confirmPasswordLayout.setVisibility(View.GONE);
        submitButton.setText(getResources().getString(R.string.login_button));
        toggleView.setText(spannable);
        toggleButton.setText(getResources().getString(R.string.switch_to_signup_button));
        currentOption = ActivityOption.LOGIN;
    }

    public void onClickSubmit(View view) {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (currentOption == ActivityOption.SIGNUP) {
            String confirmPassword = passwordInput.getText().toString();
            if (password.equals(confirmPassword)) {
                // TODO: encrypt, send to server (request new user)
            } else {
                errorView.setText(getResources().getString(R.string.error_password_match));
                errorView.setVisibility(View.VISIBLE);
            }
        } else if (currentOption == ActivityOption.LOGIN) {
            // TODO: encrypt, send to server (request login)
        }
    }

    // TODO: receive server response

    public void onClickToggle(View view) {
        if (currentOption == ActivityOption.SIGNUP) setToLogin();
        else if (currentOption == ActivityOption.LOGIN) setToSignup();
    }
}


















