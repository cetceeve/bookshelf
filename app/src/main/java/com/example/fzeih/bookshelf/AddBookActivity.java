package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBooklistDatabaseReference;

    private Button create;
    private EditText title;
    private EditText authorSurName;
    private EditText authorLastName;
    private EditText isbn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String listname = extras.getString("listname");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBooklistDatabaseReference = mFirebaseDatabase.getReference().child("booklists").child(listname);

        create = (Button) findViewById(R.id.button_create_bookitem);
        title = (EditText) findViewById(R.id.editText_title);
        authorSurName = (EditText) findViewById(R.id.editText_authorSurName);
        authorLastName = (EditText) findViewById(R.id.editText_authorLastName);
        isbn = (EditText) findViewById(R.id.editText_isbn);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastNameText = authorLastName.getText().toString();
                String surNameText = authorSurName.getText().toString();
                String titleText = title.getText().toString();
                String isbnText = isbn.getText().toString();

                Book bookItem = new Book(lastNameText,surNameText,titleText,isbnText);
                mBooklistDatabaseReference.push().setValue(bookItem);
                finish();
            }
        });
    }
}
