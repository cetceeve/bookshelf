package com.example.fzeih.bookshelf.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditBookActivity extends AppCompatActivity {
    private DatabaseReference mBookDatabaseReference;

    private Book mBook;
    private String mBookListKey;

    private EditText mTitleEditText;
    private EditText mSubtitleEditText;
    private EditText mAuthorEditText;
    private EditText mIsbnEditText;
    private EditText mPublisherEditText;
    private EditText mPublishedYearEditText;
    private EditText mPagesEditText;
    private EditText mDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);
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
        Button saveChangesButton = findViewById(R.id.button_add_edit_save_changes);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
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
        ConstraintLayout cl = findViewById(R.id.view_group_add_edit_top_bar);
        cl.setVisibility(View.GONE);

        mTitleEditText = findViewById(R.id.edit_text_add_edit_title);
        mSubtitleEditText = findViewById(R.id.edit_text_add_edit_subtitle);
        mAuthorEditText = findViewById(R.id.edit_text_add_edit_author);
        mIsbnEditText = findViewById(R.id.edit_text_add_edit_isbn);
        mPublisherEditText = findViewById(R.id.edit_text_add_edit_publisher);
        mPublishedYearEditText = findViewById(R.id.edit_text_add_edit_published_year);
        mPagesEditText = findViewById(R.id.edit_text_add_edit_pages);
        mDescriptionEditText = findViewById(R.id.edit_text_add_edit_book_description);
    }

    private void setBookData() {
        if (mBook != null) {
            mTitleEditText.setText(mBook.getTitle());
            mSubtitleEditText.setText(mBook.getSubtitle());
            mAuthorEditText.setText(mBook.getAuthor());
            mIsbnEditText.setText(mBook.getIsbn());
            mPublisherEditText.setText(mBook.getPublisher());
            mPublishedYearEditText.setText(mBook.getPublishedYear());
            mPagesEditText.setText(Integer.toString(mBook.getPages()));
            mDescriptionEditText.setText(mBook.getBookDescription());
        } else {
            Toast.makeText(EditBookActivity.this, "ERROR: No book data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadUserInput() {
        // get user input
        mBook.setTitle(mTitleEditText.getText().toString());
        mBook.setSubtitle(mSubtitleEditText.getText().toString());
        mBook.setAuthor(mAuthorEditText.getText().toString());
        mBook.setIsbn(mIsbnEditText.getText().toString());
        mBook.setPublisher(mPublisherEditText.getText().toString());
        mBook.setPublishedYear(mPublishedYearEditText.getText().toString());
        mBook.setBookDescription(mDescriptionEditText.getText().toString());

        int pages = 0;
        if (mPagesEditText.getText().toString().length() != 0) {
            pages = Integer.parseInt(mPagesEditText.getText().toString());
        }
        mBook.setPages(pages);

        // upload data
        mBookDatabaseReference.setValue(mBook);
    }
}
