package com.example.fzeih.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AchievementAdapter extends ArrayAdapter<Achievement> {
    private int mResource;

    public AchievementAdapter(@NonNull Context context, int resource, @NonNull List<Achievement> objects) {
        super(context, resource, objects);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.text_view_achievement);
        ImageView imageView = convertView.findViewById(R.id.image_view_achievement);

        Achievement achievement = getItem(position);

        textView.setText(achievement.getAchievementText());
        imageView.setImageDrawable(achievement.getDrawableResource());

        return convertView;
    }
}
