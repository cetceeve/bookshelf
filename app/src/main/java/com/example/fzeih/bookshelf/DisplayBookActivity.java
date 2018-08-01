package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayBookActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView authorNameTextView;
    private TextView isbnTextView;
    private Button editButton;
    private Book mCurrentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_book);

        Intent i = getIntent();
        Bundle extra = i.getExtras();
        mCurrentBook = (Book)extra.get("book");
        String booklistname = (String)extra.get("listname");

        getSupportActionBar().setTitle(booklistname);

        titleTextView = (TextView)findViewById(R.id.textView_title_book);
        authorNameTextView = (TextView)findViewById(R.id.textView_authorName_book);
        isbnTextView = (TextView)findViewById(R.id.textView_isbn_book);
        editButton = (Button)findViewById(R.id.button_edit);

        titleTextView.setText(mCurrentBook.getTitle());
        authorNameTextView.setText(mCurrentBook.getAuthorName());
        isbnTextView.setText(mCurrentBook.getIsbn());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayBookActivity.this,editBookActivity.class);
                intent.putExtra("book",mCurrentBook);
                startActivity(intent);
            }
        });

    }
}
