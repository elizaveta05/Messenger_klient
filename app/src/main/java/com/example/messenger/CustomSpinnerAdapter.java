package com.example.messenger;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<String> items;
    private final int mResource;

    public CustomSpinnerAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.items = items;
        this.mResource = resource;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        final View view = mInflater.inflate(mResource, parent, false);

        TextView text = view.findViewById(android.R.id.text1);
        text.setText(items.get(position));
        text.setTextColor(mContext.getResources().getColor(R.color.black)); // Установка цвета текста

        // Установка белого фона
        view.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));

        return view;
    }
}