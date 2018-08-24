package com.example.fzeih.bookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.fzeih.bookshelf.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<String> {

    public ImageAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageview_photo);
        String pathname = getItem(position);
        if (pathname != null) {
            Picasso.get().load(new File(pathname)).resize(360, 480).into(imageView);
        }
        return convertView;
    }

}
