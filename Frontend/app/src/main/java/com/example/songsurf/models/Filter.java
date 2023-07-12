package com.example.songsurf.models;

import java.util.List;
import java.util.ArrayList;
public class Filter {
    private static Filter instance;
    private List<String> selectedGenres, selectedLanguages, selectedYears;

    private Filter() {
        selectedGenres = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
        selectedYears = new ArrayList<>();
    }

    public static Filter getInstance() { // Singleton pattern to ensure only one instance of Filter is created because we only need one instance of Filter to store the selected filters
        if (instance == null) {
            instance = new Filter();
        }
        return instance;
    }

    public List<String> getSelectedGenres() {
        return selectedGenres;
    }

    public void setSelectedGenres(List<String> selectedGenres) {
        this.selectedGenres = selectedGenres;
    }

    public List<String> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(List<String> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public List<String> getSelectedYears() {
        return selectedYears;
    }

    public void setSelectedYears(List<String> selectedYears) {
        this.selectedYears = selectedYears;
    }

}
