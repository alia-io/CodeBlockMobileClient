package com.example.codeblockmobileclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.codeblockmobileclient.communication.dto.AuthPacket;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;

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
        findViews();
        setToLogin();
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
            String pwHash = BCrypt.withDefaults().hashToString(10, password.toCharArray());
            JSONObject loginRequestObj = new JSONObject();
            try {
                loginRequestObj.put("email", email);
                loginRequestObj.put("password", pwHash);
            } catch (Exception e) {
                Log.i("SignupLoginActivity", "JSONObject error");
                e.printStackTrace();
                // TODO: display error message in UI
                throw new Exception("JSONObject error");
            }

            //sendHttpPostRequest("login", loginRequestObj);
            //sendHttpGetRequest();

            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());    // use for UI stuff

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //sendHttpGetRequest();   // background work
                    sendHttpPostRequest("login", loginRequestObj);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // UI thread work here
                        }
                    });
                }
            });
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

    private Boolean sendHttpPostRequest(String urlExt, JSONObject jsonObject) {

        /*try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://...";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Title", "Android Volley Demo");
            jsonBody.put("Author", "BNK");
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        

        RequestQueue queue = Volley.newRequestQueue(this);
        Boolean result = false;
        String urlString = "http://10.0.2.2:8080/" + urlExt;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        Request<AuthPacket> request = new Request<AuthPacket>(Request.Method.POST, urlString, null) {
            @Override
            protected Response<AuthPacket> parseNetworkResponse(NetworkResponse response) {
                return null;
            }

            @Override
            protected void deliverResponse(AuthPacket response) {

            }
        };




        /*String urlString = "http://10.0.2.2:8080/" + urlExt;
        Boolean result = false;
        HttpURLConnection httpConnection = null;

        try {
            URL url = new URL(urlString);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.addRequestProperty("content-type", "application/json");
            //httpConnection.setFixedLengthStreamingMode(jsonObject.length());
            httpConnection.setChunkedStreamingMode(0);
            OutputStream out = httpConnection.getOutputStream();
            OutputStreamWriter outStream = new OutputStreamWriter(out);
            outStream.write(jsonObject.toString());
            Log.i("HttpPostRequest", "Sending post:\n" + jsonObject.toString());
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("HttpPostRequest", "line: " + line);
                }
                reader.close();
                Log.i("HttpPostRequest", "POST request returned ok");
                result = true;
            } else {
                Log.i("HttpPostRequest", "POST request returned error");
                result = false;
            }
        } catch (Exception e) {
            Log.i("HttpPostRequest", "Exception in sendHttpPostRequest");
            e.printStackTrace();
            result = false;
        }*/

        return result;
    }
}



