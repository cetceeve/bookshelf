package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBookListDatabaseReferece;
    private Button create;
    private EditText title;
    private EditText authorSurName;
    private EditText authorLastName;
    private EditText isbn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        mBookListDatabaseReferece = (DatabaseReference) extras.get("booklist_database_reference");

        create = (Button) findViewById(R.id.button_create_bookitem);
        title = (EditText) findViewById(R.id.editText_title);
        authorSurName = (EditText) findViewById(R.id.editText_authorSurName);
        authorLastName = (EditText) findViewById(R.id.editText_authorLastName);
        isbn = (EditText) findViewById(R.id.editText_isbn);

        final String lastNameText = authorLastName.getText().toString();
        final String surNameText = authorSurName.getText().toString();
        final String titleText = title.getText().toString();
        final String isbnText = isbn.getText().toString();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Book bookItem = new Book(lastNameText,surNameText,titleText,isbnText);
                mBookListDatabaseReferece.push().setValue(bookItem);
                finish();
            }
        });
    }
}
