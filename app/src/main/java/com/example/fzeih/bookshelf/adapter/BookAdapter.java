package com.example.fzeih.bookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Book;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {
    private int mResource;

    public BookAdapter(@NonNull Context context, int resource, @NonNull List<Book> objects) {
        super(context, resource, objects);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.textview_book_title);
        TextView authorTextView = convertView.findViewById(R.id.textview_book_author);
        ImageView coverImageView = convertView.findViewById(R.id.imageView_cover_bookItem);

        Book mBook = getItem(position);
        if (mBook != null) {
            if(mBook.getCoverUrl().length() != 0){
                Picasso.get().load(mBook.getCoverUrl()).into(coverImageView);
            } else {
                coverImageView.setImageResource(R.drawable.ic_book);
            }
            titleTextView.setText(mBook.getTitle());
            authorTextView.setText(mBook.getAuthor());
        }
        return convertView;
    }
}
