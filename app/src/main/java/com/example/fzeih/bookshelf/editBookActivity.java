package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class editBookActivity extends AppCompatActivity {

    private Button editButton;
    private EditText titleEditText;
    private EditText authorNameEditText;
    private EditText isbnEditText;
    private Book book;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        book = (Book)extras.get("book");

        editButton = (Button) findViewById(R.id.button_edit_bookitem);
        titleEditText = (EditText) findViewById(R.id.editText_title_edit);
        authorNameEditText = (EditText) findViewById(R.id.editText_authorName_edit);
        isbnEditText = (EditText) findViewById(R.id.editText_isbn_edit);

        titleEditText.setText(book.getTitle());
        authorNameEditText.setText(book.getAuthorName());
        isbnEditText.setText(book.getIsbn());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                book.setTitle(titleEditText.getText().toString());
                book.setAuthorName(authorNameEditText.getText().toString());
                book.setIsbn(isbnEditText.getText().toString());
                finish();
            }
        });
    }
}
