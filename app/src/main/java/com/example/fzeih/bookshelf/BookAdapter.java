package com.example.fzeih.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        Book mBook = getItem(position);
        if (mBook != null) {
            titleTextView.setText(mBook.getTitle());
            authorTextView.setText(mBook.getAuthor());
        }
        return convertView;
    }
}
