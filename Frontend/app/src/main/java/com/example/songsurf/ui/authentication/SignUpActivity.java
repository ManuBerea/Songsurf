package com.example.songsurf.ui.authentication;

import android.content.Intent;
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
import com.example.songsurf.utils.validators.EmailValidator;
import com.example.songsurf.utils.validators.PasswordValidator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText textInputEditTextUsername, textInputEditTextPassword, textInputEditTextEmail, textInputEditTextConfirmPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textInputEditTextUsername = findViewById(R.id.username);
        textInputEditTextPassword = findViewById(R.id.password);
        textInputEditTextConfirmPassword = findViewById(R.id.confirm_password);
        textInputEditTextEmail = findViewById(R.id.email);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        TextView textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonSignUp.setOnClickListener(v -> {
            final String username, email, password, confirm_password;
            username = String.valueOf(textInputEditTextUsername.getText());
            password = String.valueOf(textInputEditTextPassword.getText());
            confirm_password = String.valueOf(textInputEditTextConfirmPassword.getText());
            email = String.valueOf(textInputEditTextEmail.getText());

            if (username.equals("") || password.equals("") || email.equals("") || confirm_password.equals("")) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirm_password)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else if (!PasswordValidator.validate(password)) {
                Toast.makeText(this, "Password must be at least 8 characters, include an uppercase letter, a digit, and a special character.", Toast.LENGTH_SHORT).show();
            } else if(!EmailValidator.validate(email)){
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                registerUser(username, email, password);
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("username", username);
            requestObject.put("email", email);
            requestObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                UrlProvider.REGISTER_URL,
                requestObject,
                response -> {
                    try {
                        String message = response.getString("message");
                        if ("User created successfully.".equals(message)) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
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
