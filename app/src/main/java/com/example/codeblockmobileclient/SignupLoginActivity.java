package com.example.codeblockmobileclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.codeblockmobileclient.communication.dto.PublicKeyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.HybridConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

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

    private RequestQueue requestQueue;
    private KeysetHandle keysetHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        requestQueue = Volley.newRequestQueue(this);
        findViews();
        setToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            HybridConfig.register();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        sendHttpGetRequest("api/auth/public_key", null);
    }

    private void findViews() {
        instructionView = findViewById(R.id.instruction);
        errorView = findViewById(R.id.error_text);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        emailInput = findViewById(R.id.email_address);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        submitButton = findViewById(R.id.signup_login_button);
        toggleView = findViewById(R.id.toggle_text);
        toggleButton = findViewById(R.id.toggle_button);
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

    public void onClickToggle(View view) {
        if (currentOption == ActivityOption.SIGNUP) setToLogin();
        else if (currentOption == ActivityOption.LOGIN) setToSignup();
    }

    public void onClickSubmit(View view) throws Exception {

        Log.i("Volley", "onClick: keysetHandle = " + keysetHandle);

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
            HybridEncrypt hybridEncrypt = keysetHandle.getPrimitive(HybridEncrypt.class);
            Log.i("Volley", "hybridEncrypt = " + hybridEncrypt);
            String encryptedPass = Base64.getEncoder().encodeToString(
                    hybridEncrypt.encrypt(password.getBytes(StandardCharsets.UTF_8), null));
            JSONObject loginRequestObj = new JSONObject();
            try {
                loginRequestObj.put("email", email);
                loginRequestObj.put("password", encryptedPass);
            } catch (Exception e) {
                Log.i("SignupLoginActivity", "JSONObject error");
                e.printStackTrace();
                // TODO: display error message in UI
                throw new Exception("JSONObject error");
            }

            sendHttpPostRequest("api/auth/login", loginRequestObj);
        }
    }

    private void sendHttpGetRequest() {
        try {
            URL url = new URL("https://dog.ceo/api/breeds/image/random");
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inStream = httpConnection.getInputStream();
                if (inStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d("HttpPostRequest", "line: " + line);
                    }
                    reader.close();
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            httpConnection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendHttpGetRequest(String urlExt, JSONObject jsonObject) {

        String url = "http://10.0.2.2:8080/" + urlExt;
        Log.i("Volley", "GET request");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i("Volley", "Response:\n" + response);
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            PublicKeyDTO publicKeyDTO = objectMapper.readValue(response.toString(), PublicKeyDTO.class);
                            keysetHandle = publicKeyDTO.getPublicKeySetHandle();
                            Log.i("Volley", "publicKeySetHandle = " + keysetHandle);
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.i("Volley", "Error: " + error);
                        errorView.setText("HTTP GET Error");
                        errorView.setVisibility(View.VISIBLE);

                    }
                });

        requestQueue.add(request);
    }

    // TODO: Use a DTO object instead of a JSON object: https://medium.com/@dcortes22/android-how-to-use-intelligently-volley-a7787fb8295a
    private void sendHttpPostRequest(String urlExt, JSONObject jsonObject) {

        String url = "http://10.0.2.2:8080/" + urlExt;
        Log.i("Volley", "POST request");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley", "Response:\n" + response);
                        errorView.setText("Response received");
                        errorView.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley", "Error: " + error);
                        errorView.setText("HTTP POST Error");
                        errorView.setVisibility(View.VISIBLE);
                    }
                });

        requestQueue.add(request);
    }
}



