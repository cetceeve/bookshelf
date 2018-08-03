package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBooklistDatabaseReference;
    private String mBooklistKey;

    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBooklistKey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistKey = extras.getString(Constants.key_intent_booklistkey);
    }

    private void getDatabaseReference() {
        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklists).child(mBooklistKey);
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
