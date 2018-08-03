package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayBookActivity extends AppCompatActivity {

    private Book mBook;
    private String mBooklistKey;
    private DatabaseReference mBookDatabaseReference;

    private TextView mTitleTextView;
    private TextView mAuthorNameTextView;
    private TextView mIsbnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_book);

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
        attachDatabaseReadListener();

        Button editButton = (Button)findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditBookAcitivity();
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
        mTitleTextView = (TextView)findViewById(R.id.textView_title_book);
        mAuthorNameTextView = (TextView)findViewById(R.id.textView_authorName_book);
        mIsbnTextView = (TextView)findViewById(R.id.textView_isbn_book);
    }

    private void setBookData() {
        mTitleTextView.setText(mBook.getTitle());
        mAuthorNameTextView.setText(mBook.getAuthorName());
        mIsbnTextView.setText(mBook.getIsbn());
    }

    private void startEditBookAcitivity() {
        Intent intent = new Intent(DisplayBookActivity.this,editBookActivity.class);
        intent.putExtra(Constants.key_intent_book, mBook);
        intent.putExtra(Constants.key_intent_booklistkey, mBooklistKey);
        startActivity(intent);
    }

    private void attachDatabaseReadListener() {
        ValueEventListener bookListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mBook = dataSnapshot.getValue(Book.class);
                setBookData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBookDatabaseReference.addValueEventListener(bookListener);
    }
}
