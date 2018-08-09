package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayBookActivity extends AppCompatActivity {
    private DatabaseReference mBookDatabaseReference;
    private DatabaseReference mNumOfReadBooksDatabaseReference;
    private ValueEventListener mBookValueEventListener;
    private ValueEventListener mNumOfReadBooksValueEventListener;

    private String mBookListKey;
    private Long mNumOfReadBooks;
    private Book mBook;

    private TextView mTitleTextView;
    private TextView mAuthorNameTextView;
    private TextView mIsbnTextView;
    private Switch mBookReadSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Book Details");

        // Intent
        readIntent();

        // Views
        initViews();

        // Data
        setBookData();
        getDatabaseReference();

        // Listeners
        setSwitchStateChangeListener();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachBookDatabaseReadListener();
        detachNumOfReadBooksDatabaseReadListener();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachBookDatabaseReadListener();
        attachNumOfReadBooksDatabaseReadListener();
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
            mNumOfReadBooksDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_books_read);
        } else {
            Toast.makeText(DisplayBookActivity.this, "ERROR: User is not signed in!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        mTitleTextView = (TextView) findViewById(R.id.textView_title_book);
        mAuthorNameTextView = (TextView) findViewById(R.id.textView_authorName_book);
        mIsbnTextView = (TextView) findViewById(R.id.textView_isbn_book);
        mBookReadSwitch = (Switch) findViewById(R.id.switch_book_read);
    }

    private void setBookData() {
        if (mBook != null) {
            mTitleTextView.setText(mBook.getTitle());
            mAuthorNameTextView.setText(mBook.getAuthorName());
            mIsbnTextView.setText(mBook.getIsbn());
            mBookReadSwitch.setChecked(mBook.getRead());
        } else {
            Toast.makeText(DisplayBookActivity.this, "ERROR: No book data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSwitchStateChangeListener() {
        mBookReadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // only happens once when user calls this method for the very first time
                // reason: firebase return null, because value doesn't exist yet
                if (mNumOfReadBooks == null) {
                    mNumOfReadBooks = 0L;
                }
                // standard behaviour
                if (b) {
                    Book changedBook = new Book(mBook.getKey(), mBook.getAuthorName(), mBook.getTitle(), mBook.getIsbn(), true);
                    mBookDatabaseReference.setValue(changedBook);
                    mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks + 1);
                } else {
                    Book changedBook = new Book(mBook.getKey(), mBook.getAuthorName(), mBook.getTitle(), mBook.getIsbn(), false);
                    mBookDatabaseReference.setValue(changedBook);
                    mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks - 1);
                }
            }
        });
    }


    private void attachBookDatabaseReadListener() {
        mBookValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBook = dataSnapshot.getValue(Book.class);
                if (mBook != null) {
                    setBookData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mBookDatabaseReference.addValueEventListener(mBookValueEventListener);
    }

    private void attachNumOfReadBooksDatabaseReadListener() {
        mNumOfReadBooksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNumOfReadBooks = (Long) dataSnapshot.getValue();
                // TODO: find true place for this
                Snackbar.make(mBookReadSwitch, "you've read " + mNumOfReadBooks + " book", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mNumOfReadBooksDatabaseReference.addValueEventListener(mNumOfReadBooksValueEventListener);
    }

    private void detachBookDatabaseReadListener() {
        if (mBookValueEventListener != null) {
            mBookDatabaseReference.removeEventListener(mBookValueEventListener);
            mBookValueEventListener = null;
        }
    }

    private void detachNumOfReadBooksDatabaseReadListener() {
        if (mNumOfReadBooksValueEventListener != null) {
            mNumOfReadBooksDatabaseReference.removeEventListener(mNumOfReadBooksValueEventListener);
            mNumOfReadBooksValueEventListener = null;
        }
    }

    private void startEditBookActivity() {
        Intent intent = new Intent(DisplayBookActivity.this, EditBookActivity.class);
        intent.putExtra(Constants.key_intent_book, mBook);
        intent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display_book, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationSnackbar();
                return true;
            case R.id.action_edit:
                startEditBookActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showDeleteConfirmationSnackbar() {
        /*
        TODO: cleanup on consensus with team
        AlertDialog.Builder deleteConfirmationDialog = new AlertDialog.Builder(DisplayBookActivity.this);
        deleteConfirmationDialog.setMessage(R.string.dialog_message_delete_confirmation)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        detachBookDatabaseReadListener();
                        detachNumOfReadBooksDatabaseReadListener();
                        mBookDatabaseReference.removeValue();
                        if (mBook.getRead()) {
                            mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks - 1);
                        }
                        Toast.makeText(DisplayBookActivity.this, "Book removed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteConfirmationDialog.show();
        */
        detachBookDatabaseReadListener();
        detachNumOfReadBooksDatabaseReadListener();

        mBookDatabaseReference.removeValue();
        if (mBook.getRead()) {
            mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks - 1);
        }

        // inform listeners
        Object[] bookDeletionListeners = ListenerAdministrator.getListener(BookDeletionListener.class);
        for (Object listener: bookDeletionListeners) {
            ((BookDeletionListener) listener).bookDeleted(mBookDatabaseReference, mBook);
        }

        finish();
    }
}
