package com.example.fzeih.bookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.fzeih.bookshelf.R;

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

        final ImageView imageView = convertView.findViewById(R.id.imageview_photo);

        Uri uri = Uri.parse(getItem(position));

        Bitmap bitmap = BitmapFactory.decodeFile(uri.toString());

        imageView.setImageBitmap(bitmap);

        return convertView;
    }


}
