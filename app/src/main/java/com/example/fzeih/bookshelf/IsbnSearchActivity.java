package com.example.fzeih.bookshelf;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IsbnSearchActivity extends AppCompatActivity implements DownloadCallback {

    private EditText mIsbnEditText;
    private Button mSearchButton, mAddResultButton;
    private TextView mResultTextView;

    private String mBooklistKey;
    private DatabaseReference mBooklistDatabaseReference;

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

        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager());


        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsbnQueryInput = mIsbnEditText.getText().toString();
                mRequestUrl = BOOK_BASE_URL + QUERY_PARAM_ISBN + mIsbnQueryInput;

                startDownload();

            }
        });
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistKey = extras.getString(Constants.key_intent_booklistkey);
    }


    private void getDatabaseReference() {
        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklists).child(mBooklistKey);
    }

    private void initViews() {
        mIsbnEditText = (EditText) findViewById(R.id.edittext_isbn);
        mSearchButton = (Button) findViewById(R.id.button_searchByIsbn);
        mResultTextView = (TextView) findViewById(R.id.textView_result_isbnsearch);
        mAddResultButton = (Button) findViewById(R.id.button_add_isbnsearch);
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
        getResults(result);
        addNewBook();
    }

    private void getResults(Object result) {
        // parse JSON
        mTitle = null;
        mAuthor = null;
        try {
            JSONObject jsonObject = new JSONObject((String) result);
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                try {
                    mTitle = volumeInfo.getString("title");
                    mAuthor = volumeInfo.getString("authors");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mTitle != null && mAuthor != null) {
                    mResultTextView.setText(mTitle + "\n" + mAuthor);
                    mAddResultButton.setVisibility(View.VISIBLE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(IsbnSearchActivity.this, "Invalid ISBN! Could not find a book", Toast.LENGTH_LONG).show();
        }
    }

    private void addNewBook() {
        mAddResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // upload data
                DatabaseReference nextBookDatabaseReference = mBooklistDatabaseReference.push();
                Book bookItem = new Book(nextBookDatabaseReference.getKey(), mAuthor, mTitle, mIsbnQueryInput);
                nextBookDatabaseReference.setValue(bookItem);
                Toast.makeText(IsbnSearchActivity.this, "Added book to list", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

    private void startDownload() {
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload(mRequestUrl);
            mDownloading = true;
        }
    }
}
