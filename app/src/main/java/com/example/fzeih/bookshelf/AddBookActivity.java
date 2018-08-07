package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBooklistDatabaseReference;
    private DatabaseReference mBooksReadDatabaseReference;
    private ValueEventListener mValueEventListenerReadBooks;
    private Long mNumReadBooks;
    private String mBooklistKey;

    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;
    private Switch mBookReadSwitch;

    private boolean mBookWasRead = false;

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
        mBookReadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBookWasRead = !mBookWasRead;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachReadDatabaseListenerReadBooks();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachDatabaseReadListenerReadBooks();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistKey = extras.getString(Constants.key_intent_booklistkey);
    }

    private void getDatabaseReference() {
        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklists).child(mBooklistKey);
        mBooksReadDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_books_read);
    }

    private void attachDatabaseReadListenerReadBooks() {
        mValueEventListenerReadBooks = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNumReadBooks = (Long) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBooksReadDatabaseReference.addValueEventListener(mValueEventListenerReadBooks);
    }

    private void detachReadDatabaseListenerReadBooks() {
        if (mValueEventListenerReadBooks != null) {
            mBooksReadDatabaseReference.removeEventListener(mValueEventListenerReadBooks);
            mValueEventListenerReadBooks = null;
        }
    }

    private void initViews() {
        mTitleEditText = (EditText) findViewById(R.id.editText_title);
        mAuthorNameEditText = (EditText) findViewById(R.id.editText_authorName);
        mIsbnEditText = (EditText) findViewById(R.id.editText_isbn);
        mBookReadSwitch = findViewById(R.id.switch_book_read_add_book);
    }

    private void uploadUserInput() {
        String nameText = mAuthorNameEditText.getText().toString();
        String titleText = mTitleEditText.getText().toString();
        String isbnText = mIsbnEditText.getText().toString();

        // upload data
        DatabaseReference nextBookDatabaseReference = mBooklistDatabaseReference.push();
        Book bookItem = new Book(nextBookDatabaseReference.getKey(), nameText,titleText,isbnText, mBookWasRead);
        nextBookDatabaseReference.setValue(bookItem);
        if (mBookWasRead) {
            mBooksReadDatabaseReference.setValue(mNumReadBooks + 1);
        }
    }
}
