package com.example.fzeih.bookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.activities.WishGalleryActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<String> {
    Context mContext;

    public ImageAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId, data);
        mContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_image, parent, false);
        }

        final ImageView imageView = convertView.findViewById(R.id.imageview_photo);
        final ImageView deleteImageButton = convertView.findViewById(R.id.wish_list_delete_image);
        final ProgressBar progressBar = convertView.findViewById(R.id.progress_bar_image);

        final String pathname = getItem(position);

        int imageTargetWidth = 480;
        int imageTargetHeight = 640;

        if (pathname != null) {
            Picasso.get().load(new File(pathname)).resize(imageTargetWidth, imageTargetHeight).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    deleteImageButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    imageView.setVisibility(View.GONE);
                    deleteImageButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Something went wrong during image load!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WishGalleryActivity) mContext).removeImageFromGridview(pathname, position);
            }
        });

        return convertView;
    }

}
