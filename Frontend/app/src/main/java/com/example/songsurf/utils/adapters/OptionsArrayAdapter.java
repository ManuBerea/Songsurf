package com.example.songsurf.utils.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.songsurf.R;
import com.example.songsurf.models.Option;

import java.util.ArrayList;

public class OptionsArrayAdapter extends ArrayAdapter<Option> {

    public OptionsArrayAdapter(Context context, ArrayList<Option> options) {
        super(context, 0, options);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Option option = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_option, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.icon);
        TextView title = convertView.findViewById(R.id.title);

        icon.setImageDrawable(option.getIcon());
        title.setText(option.getTitle());

        return convertView;
    }

}
