package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditBookActivity extends AppCompatActivity {
    private DatabaseReference mBookDatabaseReference;

    private Book mBook;
    private String mBookListKey;

    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;
    private Button mEditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        getSupportActionBar().setTitle("Edit Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intent
        readIntent();

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
        if (extras != null) {
            mBook = (Book) extras.get(Constants.key_intent_book);
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
        }
    }

    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mBookDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey).child(mBook.getKey());
        } else {
            Toast.makeText(EditBookActivity.this, "ERROR: User is not signed in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        mEditButton = (Button) findViewById(R.id.button_edit_bookitem);
        mTitleEditText = (EditText) findViewById(R.id.editText_title_edit);
        mAuthorNameEditText = (EditText) findViewById(R.id.editText_authorName_edit);
        mIsbnEditText = (EditText) findViewById(R.id.editText_isbn_edit);
    }

    private void setBookData() {
        if (mBook != null) {
            mTitleEditText.setText(mBook.getTitle());
            mAuthorNameEditText.setText(mBook.getAuthorName());
            mIsbnEditText.setText(mBook.getIsbn());
        } else {
            Toast.makeText(EditBookActivity.this, "ERROR: No book data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadUserInput() {
        // get user input
        mBook.setTitle(mTitleEditText.getText().toString());
        mBook.setAuthorName(mAuthorNameEditText.getText().toString());
        mBook.setIsbn(mIsbnEditText.getText().toString());

        // upload data
        mBookDatabaseReference.setValue(mBook);
    }
}
