package com.example.fzeih.bookshelf;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IsbnSearchActivity extends AppCompatActivity implements DownloadCallback {
    private EditText mIsbnEditText;
    private Button mSearchButton, mAddResultButton;
    private TextView mResultTextView;
    private Switch mBookReadSwitch;
    private boolean mBookWasRead = false;

    private String mBooklistKey;
    private DatabaseReference mBooklistDatabaseReference;
    private DatabaseReference mBooksReadDatabaseReference;
    private ValueEventListener mValueEventListenerReadBooks;
    private Long mNumReadBooks;

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    //Parameter for URL
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    private static final String QUERY_PARAM_ISBN = "q=isbn:";
    private String mIsbnQueryInput;
    private String mRequestUrl;

    // Results of ISBN-Search
    private String mTitle;
    private String mAuthor;

    // Input from Barcode Scanner
    private String mIsbn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn_search);

        getSupportActionBar().setTitle("ISBN Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intent
        readIntent();

        // Data
        getDatabaseReference();

        // Views
        initViews();

        // Downloader
        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager());

        // Listeners
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQuery();
                startDownload();

            }
        });
        mAddResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBookData();
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
    protected void onPause() {
        super.onPause();
        detachReadDatabaseListenerReadBooks();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachDatabaseReadListenerReadBooks();

        // If called with input search immediately
        if (mIsbn != null) {
            getQuery();
            startDownload();
        }
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistKey = extras.getString(Constants.key_intent_booklistkey);
        mIsbn = extras.getString(Constants.key_intent_isbn);
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
        mIsbnEditText = (EditText) findViewById(R.id.edittext_isbn);
        mSearchButton = (Button) findViewById(R.id.button_searchByIsbn);
        mResultTextView = (TextView) findViewById(R.id.textView_result_isbnsearch);
        mAddResultButton = (Button) findViewById(R.id.button_add_isbnsearch);
        mBookReadSwitch = findViewById(R.id.switch_book_read_isbn_search);

        if (mIsbn != null) {
            mIsbnEditText.setText(mIsbn);
        }

        mBookReadSwitch.setVisibility(View.INVISIBLE);
        mAddResultButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void updateFromDownload(Object result) {
        // Update your UI here based on result of download
        if (result != null) {
            if (result.getClass() == Exception.class) {
                Toast.makeText(IsbnSearchActivity.this, "Error during search operation.", Toast.LENGTH_LONG).show();
                ((Exception) result).printStackTrace();
            } else {
                if (getResults(result.toString())) {
                    mResultTextView.setText(mTitle + "\n" + mAuthor);
                    mBookReadSwitch.setVisibility(View.VISIBLE);
                    mAddResultButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            Toast.makeText(IsbnSearchActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean getResults(String resultString) {
        // parse JSON
        mTitle = null;
        mAuthor = null;
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                mTitle = volumeInfo.getString("title");
                mAuthor = cleanAuthorString(volumeInfo.getString("authors"));
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(IsbnSearchActivity.this, "Invalid ISBN! No book found.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                // ...
                break;
            case Progress.CONNECT_SUCCESS:
                // ...
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                // ...
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                // ...
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                // ...
                break;
        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }

    private void getQuery() {
        mIsbnQueryInput = mIsbnEditText.getText().toString();
        mRequestUrl = BOOK_BASE_URL + QUERY_PARAM_ISBN + mIsbnQueryInput;
    }

    private void startDownload() {
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload(mRequestUrl);
            mDownloading = true;
        }
    }

    private void uploadBookData() {
        DatabaseReference nextBookDatabaseReference = mBooklistDatabaseReference.push();
        Book bookItem = new Book(nextBookDatabaseReference.getKey(), mAuthor, mTitle, mIsbnQueryInput, mBookWasRead);
        nextBookDatabaseReference.setValue(bookItem);
        Toast.makeText(IsbnSearchActivity.this, "Added book to list", Toast.LENGTH_SHORT).show();
        if (mBookWasRead) {
            mBooksReadDatabaseReference.setValue(mNumReadBooks + 1);
        }
    }

    private String cleanAuthorString(String string) {
        string = string.substring(1, string.length() - 1);
        String[] strings = string.split("\"");
        String res = strings[0];
        for (int i = 1; i < strings.length; i++) {
            res += strings[i];
        }
        res.trim();
        char[] chars = res.toCharArray();
        res = "";
        for (char c : chars) {
            res += c;
            if (c == ',') {
                res += " ";
            }
        }
        return res;
    }
}
