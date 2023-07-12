package com.example.songsurf.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.songsurf.models.Filter;
import com.example.songsurf.R;
import com.example.songsurf.config.UrlProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterActivity extends AppCompatActivity {
    private List<String> selectedGenres, selectedLanguages, selectedYears;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        selectedGenres = getFilterSelection("selectedGenres");
        selectedLanguages = getFilterSelection("selectedLanguages");
        selectedYears = getFilterSelection("selectedYears");

        fetchDynamicFilters(selectedGenres, selectedLanguages, selectedYears);
    }
    private List<String> getFilterSelection(String key) {
        Filter filterSelection = Filter.getInstance();
        switch(key) {
            case "selectedGenres":
                return filterSelection.getSelectedGenres();
            case "selectedLanguages":
                return filterSelection.getSelectedLanguages();
            case "selectedYears":
                return filterSelection.getSelectedYears();
            default:
                return new ArrayList<>();
        }
    }
    private void saveFilterSelection(String key, List<String> selections) {
        Filter filterSelection = Filter.getInstance(); // Singleton instance to store filter selections across activities
        switch(key) {
            case "selectedGenres":
                filterSelection.setSelectedGenres(selections);
                break;
            case "selectedLanguages":
                filterSelection.setSelectedLanguages(selections);
                break;
            case "selectedYears":
                filterSelection.setSelectedYears(selections);
                break;
        }
    }

    public void fetchDynamicFilters(List<String> selectedGenres, List<String> selectedLanguages, List<String> selectedYears) {
        String url = UrlProvider.FILTER_URL + "/dynamic";

        // Prepare the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("genres", new JSONArray(selectedGenres));
            requestBody.put("languages", new JSONArray(selectedLanguages));
            requestBody.put("years", new JSONArray(selectedYears));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        JSONArray genres = response.getJSONArray("genres");
                        ArrayList<String> genresArray = new ArrayList<>();
                        for (int i = 0; i < genres.length(); i++) {
                            genresArray.add(genres.getString(i));
                        }

                        JSONArray languages = response.getJSONArray("languages");
                        ArrayList<String> languagesArray = new ArrayList<>();
                        for (int i = 0; i < languages.length(); i++) {
                            languagesArray.add(languages.getString(i));
                        }

                        JSONArray years = response.getJSONArray("years");
                        ArrayList<String> yearsArray = new ArrayList<>();
                        for (int i = 0; i < years.length(); i++) {
                            yearsArray.add(years.getString(i));
                        }

                        genresArray.sort(String::compareToIgnoreCase);
                        languagesArray.sort(String::compareToIgnoreCase);
                        yearsArray.sort((o1, o2) -> Integer.parseInt(o2) - Integer.parseInt(o1));
                        populateChipGroups(genresArray, languagesArray, yearsArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences sharedPreferences = getSharedPreferences("com.example.songsurf", Context.MODE_PRIVATE);
                String jwt_token = sharedPreferences.getString("accessToken", null);
                headers.put("Authorization", "Bearer " + jwt_token);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }


    private void populateChipGroups(ArrayList<String> genres, ArrayList<String> languages, ArrayList<String> years) {
        ChipGroup genreChipGroup = findViewById(R.id.genreChipGroup);
        ChipGroup languageChipGroup = findViewById(R.id.languageChipGroup);
        ChipGroup yearChipGroup = findViewById(R.id.yearChipGroup);

        genreChipGroup.removeAllViews();
        languageChipGroup.removeAllViews();
        yearChipGroup.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String genre : genres) {
            Chip chip = (Chip) inflater.inflate(R.layout.my_chip, genreChipGroup, false);
            chip.setText(genre);
            chip.setCheckable(false);

            if (selectedGenres.contains(genre)) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
            }

            chip.setOnClickListener(v -> {
                if (selectedGenres.contains(genre)) {
                    selectedGenres.remove(genre);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
                } else {
                    selectedGenres.add(genre);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                }
                saveFilterSelection("selectedGenres", selectedGenres);
                fetchDynamicFilters(selectedGenres, selectedLanguages, selectedYears);
            });
            genreChipGroup.addView(chip);
        }

        for (String language : languages) {
            Chip chip = (Chip) inflater.inflate(R.layout.my_chip, languageChipGroup, false);
            chip.setText(language);
            chip.setCheckable(false);

            if (selectedLanguages.contains(language)) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
            }

            chip.setOnClickListener(v -> {
                if (selectedLanguages.contains(language)) {
                    selectedLanguages.remove(language);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
                } else {
                    selectedLanguages.add(language);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                }
                saveFilterSelection("selectedLanguages", selectedLanguages);
                fetchDynamicFilters(selectedGenres, selectedLanguages, selectedYears);
            });

            languageChipGroup.addView(chip);
        }

        for (String year : years) {
            Chip chip = (Chip) inflater.inflate(R.layout.my_chip, yearChipGroup, false);
            chip.setText(year);
            chip.setCheckable(false);

            if (selectedYears.contains(year)) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
            }

            chip.setOnClickListener(v -> {
                if (selectedYears.contains(year)) {
                    selectedYears.remove(year);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.color_when_not_checked)));
                } else {
                    selectedYears.add(year);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                }
                saveFilterSelection("selectedYears", selectedYears);
                fetchDynamicFilters(selectedGenres, selectedLanguages, selectedYears);
            });

            yearChipGroup.addView(chip);
        }
    }


    public void applyFilters(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selectedGenres", (ArrayList<String>) selectedGenres);
        resultIntent.putStringArrayListExtra("selectedLanguages", (ArrayList<String>) selectedLanguages);
        resultIntent.putStringArrayListExtra("selectedYears", (ArrayList<String>) selectedYears);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
