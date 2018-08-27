package com.example.fzeih.bookshelf.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.fzeih.bookshelf.datastructures.BookListInformation;

import java.util.List;

public class BookListInformationAdapter extends ArrayAdapter<BookListInformation> {
    private int mResource;

    public BookListInformationAdapter(@NonNull Context context, int resource, @NonNull List<BookListInformation> objects) {
        super(context, resource, objects);
        mResource = resource;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(mResource, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setTextAppearance(android.R.style.TextAppearance_Material_Medium);

        BookListInformation bookListInformation = getItem(position);
        if (bookListInformation != null) {
            textView.setText(bookListInformation.getBookListName());
        }
        return convertView;
    }
}
