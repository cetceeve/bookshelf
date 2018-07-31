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

        Book book = getItem(position);

        titleTextView.setText(book.getTitle());
        String author = book.getAuthorSurName() + " " + book.getAuthorLastName();
        authorTextView.setText(author);

        return convertView;
    }
}
