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

public class editBookActivity extends AppCompatActivity {
    private DatabaseReference mBookDatabaseReference;

    private Button mEditButton;
    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;
    private Book mBook;
    private String mBooklistKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBooklistKey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Views
        initViews();

        // Data
        setBookData();
        getDatabaseReference();

        // Listeners
        mEditButton.setOnClickListener(new View.OnClickListener() {
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
        mBook = (Book) extras.get(Constants.key_intent_book);
        mBooklistKey = extras.getString(Constants.key_intent_booklistkey);
    }

    private void getDatabaseReference() {
        mBookDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklists).child(mBooklistKey).child(mBook.getKey());
    }

    private void initViews() {
        mEditButton = (Button) findViewById(R.id.button_edit_bookitem);
        mTitleEditText = (EditText) findViewById(R.id.editText_title_edit);
        mAuthorNameEditText = (EditText) findViewById(R.id.editText_authorName_edit);
        mIsbnEditText = (EditText) findViewById(R.id.editText_isbn_edit);
    }

    private void setBookData() {
        mTitleEditText.setText(mBook.getTitle());
        mAuthorNameEditText.setText(mBook.getAuthorName());
        mIsbnEditText.setText(mBook.getIsbn());
    }

    private void uploadUserInput() {
        mBook.setTitle(mTitleEditText.getText().toString());
        mBook.setAuthorName(mAuthorNameEditText.getText().toString());
        mBook.setIsbn(mIsbnEditText.getText().toString());
        mBookDatabaseReference.setValue(mBook);
    }
}
