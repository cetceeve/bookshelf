package com.example.fzeih.bookshelf;

import android.app.Activity;
import android.content.Context;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class PhotoAdapter extends ArrayAdapter<File> {

    public PhotoAdapter(Context context, int layoutResourceId, ArrayList<File> data) {
        super(context, layoutResourceId, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageview_photo);

        File file = getItem(position);
        Uri uri = Uri.fromFile(file);


        imageView.setImageURI(uri);

        return convertView;
    }

}
