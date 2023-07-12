package com.example.songsurf.ui.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.ui.MainActivity;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
public class LoginActivity extends AppCompatActivity {
    private TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);

        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            final String email, password;
            email = String.valueOf(textInputEditTextEmail.getText());
            password = String.valueOf(textInputEditTextPassword.getText());

            if (email.equals("") || password.equals("")) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("email", email);
            requestObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                UrlProvider.LOGIN_URL,
                requestObject,
                response -> {
                    try {
                        String accessToken = response.getString("access_token");
                        SharedPreferences sharedPreferences = getSharedPreferences("com.example.songsurf", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("accessToken", accessToken).apply();

                        Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                            JSONObject errorObject = new JSONObject(json);
                            String errorMessage = errorObject.getString("message");
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}