package com.example.songsurf.models;

import android.graphics.drawable.Drawable;
import android.view.View;

public class Option {
    private final String title;
    private final Drawable icon;
    private final View.OnClickListener listener;

    public Option(String title, Drawable icon, View.OnClickListener listener) {
        this.title = title;
        this.icon = icon;
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
