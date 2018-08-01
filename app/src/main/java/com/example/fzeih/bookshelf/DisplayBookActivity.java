package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayBookActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView authorNameTextView;
    private TextView isbnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_book);

        Intent i = getIntent();
        Bundle extra = i.getExtras();
        Book currentBook = (Book)extra.get("book");

        titleTextView = (TextView)findViewById(R.id.textView_title_book);
        authorNameTextView = (TextView)findViewById(R.id.textView_authorName_book);
        isbnTextView = (TextView)findViewById(R.id.textView_isbn_book);

        titleTextView.setText(currentBook.getTitle());
        authorNameTextView.setText(currentBook.getAuthorName());
        isbnTextView.setText(currentBook.getIsbn());

    }
}
