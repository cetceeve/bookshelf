package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBookListDatabaseReference;

    private String mBookListKey;

    private EditText mTitleEditText;
    private EditText mAuthorNameEditText;
    private EditText mIsbnEditText;
    private Switch mBookReadSwitch;

    private boolean mBookWasRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        getSupportActionBar().setTitle("Add Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intent
        readIntent();

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

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
        }
    }

    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mBookListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey);
        } else {
            Toast.makeText(AddBookActivity.this, "ERROR: user is not signed in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        mTitleEditText = (EditText) findViewById(R.id.editText_title);
        mAuthorNameEditText = (EditText) findViewById(R.id.editText_authorName);
        mIsbnEditText = (EditText) findViewById(R.id.editText_isbn);
        mBookReadSwitch = findViewById(R.id.switch_book_read_add_book);
    }

    private void uploadUserInput() {
        // get user input
        String nameText = mAuthorNameEditText.getText().toString();
        String titleText = mTitleEditText.getText().toString();
        String isbnText = mIsbnEditText.getText().toString();

        // upload data
        DatabaseReference nextBookDatabaseReference = mBookListDatabaseReference.push();
        Book nextBook = new Book(nextBookDatabaseReference.getKey(), nameText, titleText, isbnText, mBookWasRead);
        nextBookDatabaseReference.setValue(nextBook);
        if (mBookWasRead) {
            DatabaseService.getInstance().getAchievementService().incrementNumOfReadBooks();
        }
    }
}
