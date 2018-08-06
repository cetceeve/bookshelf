package com.example.fzeih.bookshelf;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private Switch mBookRead;
    private ValueEventListener mValueEventListener;

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
        mTitleTextView = (TextView) findViewById(R.id.textView_title_book);
        mAuthorNameTextView = (TextView) findViewById(R.id.textView_authorName_book);
        mIsbnTextView = (TextView) findViewById(R.id.textView_isbn_book);
        mBookRead = (Switch) findViewById(R.id.switch_book_read);
        mBookRead.setChecked(mBook.getRead());

        mBookRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mBook.setRead(true);
                }else{
                    mBook.setRead(false);
                }
            }
        });
    }

    private void setBookData() {
        mTitleTextView.setText(mBook.getTitle());
        mAuthorNameTextView.setText(mBook.getAuthorName());
        mIsbnTextView.setText(mBook.getIsbn());
    }

    private void startEditBookAcitivity() {
        Intent intent = new Intent(DisplayBookActivity.this, EditBookActivity.class);
        intent.putExtra(Constants.key_intent_book, mBook);
        intent.putExtra(Constants.key_intent_booklistkey, mBooklistKey);
        startActivity(intent);
    }


    private void attachDatabaseReadListener() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mBook = dataSnapshot.getValue(Book.class);
                if (mBook != null) {
                    setBookData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBookDatabaseReference.addValueEventListener(mValueEventListener);
    }

    private void detachReadDatabaseListener() {
        if (mValueEventListener != null) {
            mBookDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_book, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                startEditBookAcitivity();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder deleteConfirmationDialog = new AlertDialog.Builder(DisplayBookActivity.this);
        deleteConfirmationDialog.setMessage(R.string.dialog_message_delete_confirmation)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        detachReadDatabaseListener();
                        mBookDatabaseReference.removeValue();
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
    }
}
