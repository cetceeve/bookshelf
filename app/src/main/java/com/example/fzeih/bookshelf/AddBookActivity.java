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

    private DatabaseReference mBooklistDatabaseReference;
    private String mBooklistName;

    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBooklistName);

        // Data
        getDatabaseReference();

        // Views
        initViews();

        // Listeners
        Button createButton = (Button) findViewById(R.id.button_create_bookitem);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadUserInput();
                finish();
            }
        });
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistName = extras.getString(Constants.key_intent_booklistname);
    }

    private void getDatabaseReference() {
        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.key_db_reference_booklists).child(mBooklistName);
    }

    private void initViews() {
        mTitleEditText = (EditText) findViewById(R.id.editText_title);
        mAuthorNameEditText = (EditText) findViewById(R.id.editText_authorName);
        mIsbnEditText = (EditText) findViewById(R.id.editText_isbn);
    }

    private void uploadUserInput() {
        String nameText = mAuthorNameEditText.getText().toString();
        String titleText = mTitleEditText.getText().toString();
        String isbnText = mIsbnEditText.getText().toString();

        // upload data
        DatabaseReference nextBookDatabaseReference = mBooklistDatabaseReference.push();
        Book bookItem = new Book(nextBookDatabaseReference.getKey(), nameText,titleText,isbnText);
        nextBookDatabaseReference.setValue(bookItem);
    }
}
