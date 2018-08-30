package com.example.fzeih.bookshelf.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Achievement;

import java.util.List;

public class AchievementAdapter extends ArrayAdapter<Achievement> {
    private int mResource;

    public AchievementAdapter(@NonNull Context context, int resource, @NonNull List<Achievement> objects) {
        super(context, resource, objects);
        mResource = resource;
    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.text_view_achievement);
        ImageView imageView = convertView.findViewById(R.id.image_view_achievement);

        Achievement achievement = getItem(position);

        if (achievement != null) {
            textView.setText(achievement.getAchievementText());

            if (!achievement.getColored()) {
                textView.setTextColor(parent.getContext().getResources().getColor(R.color.colorGrey));
            }

            imageView.setImageDrawable(achievement.getDrawableResource());
        }

        return convertView;
    }
}
