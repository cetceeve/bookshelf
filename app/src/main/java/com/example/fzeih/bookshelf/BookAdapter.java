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
    Book mBook;

    public BookAdapter(@NonNull Context context, int resource, @NonNull List<Book> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_book, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.textview_book_title);
        TextView authorTextView = convertView.findViewById(R.id.textview_book_author);

        mBook = getItem(position);

        titleTextView.setText(mBook.getTitle());
        String author = mBook.getAuthorName();
        authorTextView.setText(author);

        return convertView;
    }
}
