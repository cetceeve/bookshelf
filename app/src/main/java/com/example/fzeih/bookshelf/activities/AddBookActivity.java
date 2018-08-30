package com.example.fzeih.bookshelf.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBookListDatabaseReference;

    private String mBookListKey;

    private Switch mBookReadSwitch;
    private boolean mBookWasRead = false;

    private EditText mTitleEditText, mSubtitleEditText, mAuthorEditText, mIsbnEditText, mPublisherEditText, mPublishedYearEditText, mPagesEditText, mDescriptionEditText;
    private Button mCreateButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);
        getSupportActionBar().setTitle("Add Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intent
        readIntent();

        // Data
        getDatabaseReference();

        // Views
        initViews();

        // Listeners
        mCreateButton.setOnClickListener(new View.OnClickListener() {
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
        mBookReadSwitch = findViewById(R.id.switch_book_read_add_edit_book);

        mTitleEditText = findViewById(R.id.edit_text_add_edit_title);
        mSubtitleEditText = findViewById(R.id.edit_text_add_edit_subtitle);
        mAuthorEditText = findViewById(R.id.edit_text_add_edit_author);
        mIsbnEditText = findViewById(R.id.edit_text_add_edit_isbn);
        mPublisherEditText = findViewById(R.id.edit_text_add_edit_publisher);
        mPublishedYearEditText = findViewById(R.id.edit_text_add_edit_published_year);
        mPagesEditText = findViewById(R.id.edit_text_add_edit_pages);
        mDescriptionEditText = findViewById(R.id.edit_text_add_edit_book_description);

        mCreateButton = findViewById(R.id.button_add_edit_save_changes);
        mCreateButton.setText(R.string.text_button_create_book);
    }

    private void uploadUserInput() {
        // get user input
        String title = mTitleEditText.getText().toString();
        String subtitle = mSubtitleEditText.getText().toString();
        String author = mAuthorEditText.getText().toString();
        String isbn = mIsbnEditText.getText().toString();
        String publisher = mPublisherEditText.getText().toString();
        String publishedYear = mPublishedYearEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString();

        int pages = 0;
        if (mPagesEditText.getText().toString().length() != 0) {
            pages = Integer.parseInt(mPagesEditText.getText().toString());
        }

        // upload data
        DatabaseReference nextBookDatabaseReference = mBookListDatabaseReference.push();
        Book nextBook = new Book(nextBookDatabaseReference.getKey(), mBookWasRead, "", title, subtitle, author, isbn, publisher, publishedYear, pages, description);
        nextBookDatabaseReference.setValue(nextBook);

        DatabaseService.getInstance().getBookService().incrementTotalNumOfBooks();
        if (mBookWasRead) {
            DatabaseService.getInstance().getAchievementService().incrementNumOfReadBooks();
        }
    }
}
