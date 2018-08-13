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

public class BookListInformationAdapter extends ArrayAdapter<BookListInformation> {
    private int mResource;

    public BookListInformationAdapter(@NonNull Context context, int resource, @NonNull List<BookListInformation> objects) {
        super(context, resource, objects);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        BookListInformation bookListInformation = getItem(position);
        if (bookListInformation != null) {
            textView.setText(bookListInformation.getBookListName());
        }
        return convertView;
    }
}
