package com.example.fzeih.bookshelf.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Achievement;
import com.example.fzeih.bookshelf.datastructures.Book;
import com.example.fzeih.bookshelf.listener.AchievementServiceCallback;
import com.example.fzeih.bookshelf.listener.BookDeletionCallback;
import com.example.fzeih.bookshelf.listener.ListenerAdministrator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DisplayBookActivity extends AppCompatActivity implements AchievementServiceCallback {
    private DatabaseReference mBookDatabaseReference;
    private ValueEventListener mBookValueEventListener;

    private String mBookListKey;
    private Book mBook;

    private ImageView mCoverImageView;
    private TextView mTitleTextView, mAuthorNameTextView, mIsbnTextView, mPageNumTextView, mDescriptionTextView, mPublischerAndDateTextView, mIsbnTitleTextView, mPageTitleTextView, mDescriptionTitleTextView, mPublishedTitleTextView;


    private Switch mBookReadSwitch;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

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
        detachSwitchStateChangeListener();
        ListenerAdministrator.getInstance().removeListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachBookDatabaseReadListener();
        attachSwitchStateChangeListener();
        ListenerAdministrator.getInstance().registerListener(this);
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
            Toast.makeText(DisplayBookActivity.this, "ERROR: User is not signed in!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        mCoverImageView = (ImageView) findViewById(R.id.imageView_cover_displayBook);
        mTitleTextView = (TextView) findViewById(R.id.textView_title_book);
        mAuthorNameTextView = (TextView) findViewById(R.id.textView_authorName_book);
        mIsbnTextView = (TextView) findViewById(R.id.textView_isbn_book);
        mPageNumTextView = (TextView) findViewById(R.id.textView_num_pages);
        mPublischerAndDateTextView = (TextView)findViewById(R.id.textView_publischer_and_date);
        mDescriptionTextView = (TextView) findViewById(R.id.textView_description);
        mBookReadSwitch = (Switch) findViewById(R.id.switch_book_read);
        mIsbnTitleTextView = (TextView) findViewById(R.id.textView_ISBN_title);
        mDescriptionTitleTextView = (TextView) findViewById(R.id.textView_description_title);
        mPublishedTitleTextView = (TextView) findViewById(R.id.textView_published_title);
        mPageTitleTextView = (TextView) findViewById(R.id.textView_pages_title);
    }

    private void setBookData() {
        if (mBook != null) {
            if (mBook.getCoverUrl().length() != 0){
                Picasso.get().load(mBook.getCoverUrl()).into(mCoverImageView);
            } else {
                mCoverImageView.setImageResource(R.drawable.ic_book);
            }
            if (mBook.getTitle().length() != 0){
                mTitleTextView.setText(mBook.getTitle());
            } else {
                mTitleTextView.setVisibility(View.GONE);
            }
            if (mBook.getIsbn().length() != 0){
                mIsbnTextView.setText(mBook.getIsbn());
            } else {
                mIsbnTitleTextView.setVisibility(View.GONE);
                mIsbnTextView.setVisibility(View.GONE);
            }
            if (mBook.getPages() != 0){
                mPageNumTextView.setText(String.valueOf(mBook.getPages()));
            } else {
                mPageTitleTextView.setVisibility(View.GONE);
                mPageNumTextView.setVisibility(View.GONE);
            }
            if (mBook.getPublisherWithPublishedYear().length() != 0){
                mPublischerAndDateTextView.setText(mBook.getPublisherWithPublishedYear());
            } else {
                mPublishedTitleTextView.setVisibility(View.GONE);
                mPublischerAndDateTextView.setVisibility(View.GONE);
            }
            if (mBook.getBookDescription().length() != 0){
                mDescriptionTextView.setText(mBook.getBookDescription());
            } else {
                mDescriptionTitleTextView.setVisibility(View.GONE);
                mDescriptionTextView.setVisibility(View.GONE);
            }
            if (mBook.getAuthor().length() != 0){
                mAuthorNameTextView.setText(mBook.getAuthor());
            } else {
                mAuthorNameTextView.setVisibility(View.GONE);
            }
            ;

            // move switch without triggering the onSwitchStateChangeListener
            detachSwitchStateChangeListener();
            mBookReadSwitch.setChecked(mBook.getRead());
            attachSwitchStateChangeListener();
        } else {
            Toast.makeText(DisplayBookActivity.this, "ERROR: No book data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachSwitchStateChangeListener() {
        mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Book changedBook = new Book(mBook.getKey(), true, mBook.getCoverUrl(), mBook.getTitle(), mBook.getAuthor(), mBook.getIsbn(), mBook.getPublisher(), mBook.getPublishedYear(), mBook.getPages(), mBook.getBookDescription());
                    mBookDatabaseReference.setValue(changedBook);
                    DatabaseService.getInstance().getAchievementService().incrementNumOfReadBooks();
                } else {
                    Book changedBook = new Book(mBook.getKey(), false, mBook.getCoverUrl(), mBook.getTitle(), mBook.getAuthor(), mBook.getIsbn(), mBook.getPublisher(), mBook.getPublishedYear(), mBook.getPages(), mBook.getBookDescription());
                    mBookDatabaseReference.setValue(changedBook);
                    DatabaseService.getInstance().getAchievementService().decrementNumOfReadBooks();
                }
            }
        };
        mBookReadSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private void detachSwitchStateChangeListener() {
        if (mOnCheckedChangeListener != null) {
            mBookReadSwitch.setOnCheckedChangeListener(null);
            mOnCheckedChangeListener = null;
        }
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

    private void detachBookDatabaseReadListener() {
        if (mBookValueEventListener != null) {
            mBookDatabaseReference.removeEventListener(mBookValueEventListener);
            mBookValueEventListener = null;
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
        detachBookDatabaseReadListener();
        mBookDatabaseReference.removeValue();
        DatabaseService.getInstance().getBookService().decrementTotalNumOfBooks();
        // inform listeners
        Object[] bookDeletionListeners = ListenerAdministrator.getInstance().getListener(BookDeletionCallback.class);
        for (Object listener: bookDeletionListeners) {
            ((BookDeletionCallback) listener).bookDeleted(mBookDatabaseReference, mBook);
        }
        finish();
    }

    @Override
    public void onNumOfReadBooksChanged(@NonNull Long numOfReadBooks) {
    }

    @Override
    public void onAchievementChanged(Achievement highestAchievement) {
        if (highestAchievement!= null) {
            Snackbar.make(mBookReadSwitch, highestAchievement.getAchievementText(), Snackbar.LENGTH_LONG)
                    .setAction("Show Profile", new ShowProfileListener()).show();
        }
    }

    private class ShowProfileListener implements View.OnClickListener{

        private ShowProfileListener() {
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DisplayBookActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
    }
}