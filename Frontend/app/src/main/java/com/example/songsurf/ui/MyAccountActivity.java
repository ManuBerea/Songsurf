package com.example.songsurf.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.example.songsurf.models.Option;
import com.example.songsurf.ui.authentication.LoginActivity;
import com.example.songsurf.ui.playlist.MyPlaylistsActivity;
import com.example.songsurf.utils.adapters.OptionsArrayAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        showBottomNavigationMenu();

        ArrayList<Option> options = new ArrayList<>();
        options.add(new Option("Change Username", ContextCompat.getDrawable(this, R.drawable.ic_change), v -> showChangeUsernameDialog()));
        options.add(new Option("Change Email", ContextCompat.getDrawable(this, R.drawable.ic_change), v -> showChangeEmailDialog()));
        options.add(new Option("Change Password", ContextCompat.getDrawable(this, R.drawable.ic_change), v -> showChangePasswordDialog()));
        options.add(new Option ("Delete Account", ContextCompat.getDrawable(this, R.drawable.ic_delete_account), v -> showDeleteAccountConfirmationDialog()));

        OptionsArrayAdapter optionsAdapter = new OptionsArrayAdapter(this, options);
        ListView optionsListView = findViewById(R.id.options_list);
        optionsListView.setAdapter(optionsAdapter);

        optionsListView.setOnItemClickListener((parent, view, position, id) -> {
            Option option = optionsAdapter.getItem(position);
            option.getListener().onClick(view);
        });

        findViewById(R.id.sign_out_button).setOnClickListener(v -> showSignOutConfirmationDialog());

        getCurrentUsername();

        ImageView profilePictureImageView = findViewById(R.id.profile_picture);
        profilePictureImageView.setImageResource(R.drawable.ic_profile_picture);
    }

    @SuppressLint("NonConstantResourceId")
    private void showBottomNavigationMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.navigation_more);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_songsurf:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    return true;
                case R.id.navigation_playlists:
                    startActivity(new Intent(getApplicationContext(), MyPlaylistsActivity.class));
                    return true;
                case R.id.navigation_more:
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_more);
    }

    private void showChangeUsernameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.text_input_dialog, findViewById(android.R.id.content), false);
        EditText newUsername = dialogView.findViewById(R.id.input);
        newUsername.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Change Username")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, whichButton) -> changeUsername(newUsername.getText().toString()))
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss()).show();
    }

    private void showChangeEmailDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_email_dialog, findViewById(android.R.id.content), false);
        EditText newEmail = dialogView.findViewById(R.id.new_email);
        EditText password = dialogView.findViewById(R.id.password);

        new AlertDialog.Builder(this)
                .setTitle("Change Email")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, whichButton) -> changeEmail(newEmail.getText().toString(), password.getText().toString()))
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss()).show();
    }

    @SuppressLint("MissingInflatedId")
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_password_dialog, findViewById(android.R.id.content), false);
        EditText currentPassword = dialogView.findViewById(R.id.current_password);
        EditText newPassword = dialogView.findViewById(R.id.new_password);
        EditText repeatNewPassword = dialogView.findViewById(R.id.repeat_new_password);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, whichButton) -> changePassword(currentPassword.getText().toString(), newPassword.getText().toString(), repeatNewPassword.getText().toString()))
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss()).show();
    }

    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, whichButton) -> logoutUser())
                .setNegativeButton("No", null).show();
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, whichButton) -> deleteAccount())
                .setNegativeButton("No", null).show();
    }

    private JSONObject makeRequestObject(String... parameters) {
        JSONObject requestObject = new JSONObject();
        try {
            for (int i = 0; i < parameters.length; i += 2) {
                requestObject.put(parameters[i], parameters[i + 1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestObject;
    }

    private Map<String, String> makeHeaders() {
        Map<String, String> headers = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.songsurf", Context.MODE_PRIVATE);
        String jwt_token = sharedPreferences.getString("accessToken", null);
        headers.put("Authorization", "Bearer " + jwt_token);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private void handleResponse(Response.Listener<JSONObject> responseListener, String url, int method, JSONObject requestObject) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, requestObject, responseListener, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                return makeHeaders();
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String errorMessage = new JSONObject(new String(error.networkResponse.data)).getString("message");
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.songsurf", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void getCurrentUsername() {
        handleResponse(response -> {
            try {
                String username = response.getString("username");
                TextView userNameTextView = findViewById(R.id.user_name);
                userNameTextView.setText(username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.GET_USERNAME_URL, Request.Method.GET, null);
    }

    private void changeUsername(String newUsername) {
        handleResponse(response -> {
            try {
                String message = response.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                getCurrentUsername();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.CHANGE_USERNAME_URL, Request.Method.PUT, makeRequestObject("new_username", newUsername));
    }

    private void changeEmail(String newEmail, String password) {
        handleResponse(response -> {
            try {
                String message = response.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.CHANGE_EMAIL_URL, Request.Method.PUT, makeRequestObject("new_email", newEmail, "password", password));
    }

    private void changePassword(String currentPassword, String newPassword, String repeatNewPassword) {
        handleResponse(response -> {
            try {
                String message = response.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.CHANGE_PASSWORD_URL, Request.Method.PUT, makeRequestObject("current_password", currentPassword, "new_password", newPassword, "repeat_new_password", repeatNewPassword));
    }

    private void logoutUser() {
        handleResponse(response -> {
            try {
                String message = response.getString("message");
                resetUserSession();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.LOGOUT_URL, Request.Method.POST, null);
    }

    private void deleteAccount() {
        handleResponse(response -> {
            try {
                String message = response.getString("message");
                resetUserSession();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, UrlProvider.DELETE_ACCOUNT_URL, Request.Method.DELETE, null);
    }

}
