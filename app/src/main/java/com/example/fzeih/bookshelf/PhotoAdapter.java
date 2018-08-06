package com.example.fzeih.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class PhotoAdapter extends ArrayAdapter<Uri> {

    public PhotoAdapter(Context context, int layoutResourceId, ArrayList<Uri> data) {
        super(context, layoutResourceId, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageview_photo);

        Uri uri = getItem(position);

        imageView.setImageURI(uri);

        return convertView;
    }

}
