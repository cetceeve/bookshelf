package com.example.fzeih.bookshelf.activities;

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
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.datastructures.Book;
import com.example.fzeih.bookshelf.network_fragment.DownloadCallback;
import com.example.fzeih.bookshelf.network_fragment.NetworkFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

        /*
        reference to Google Books API:
        https://developers.google.com/books/docs/overview
         */

public class IsbnSearchActivity extends AppCompatActivity implements DownloadCallback {
    //Parameter for URL
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    private static final String QUERY_PARAM_ISBN = "q=isbn:";

    private DatabaseReference mBookListDatabaseReference;

    private String mBookListKey;

    private EditText mIsbnEditText;
    private Button mSearchButton, mAddResultButton;
    private TextView mResultTitleTextView, mResultAuthorTextView;
    private Switch mBookReadSwitch;
    private ProgressBar mProgressBar;

    private boolean mBookWasRead = false;

    private NetworkFragment mNetworkFragment;
    private boolean mDownloading = false;

    // Results of ISBN-Search
    private String mTitle;
    private String mSubtitle;
    private String mAuthor;
    private String mPublisher;
    private String mPublishedYear;
    private int mPages;
    private String mDescription;
    private String mCoverUrl;

    // Input from Barcode Scanner
    private String mIsbn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn_search);
        getSupportActionBar().setTitle(R.string.title_activity_isbn_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intent
        readIntent();

        // Data
        getDatabaseReference();
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager());

        // Views
        initViews();

        // Listeners
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResults();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // if called with input from BarcodeScannerActivity, search immediately
        if (mIsbn != null) {
            startDownload();
        }
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
            mIsbn = extras.getString(Constants.key_intent_isbn);
        }
    }


    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mBookListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey);
        } else {
            Toast.makeText(IsbnSearchActivity.this, "ERROR: User is not signed in!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        mIsbnEditText = (EditText) findViewById(R.id.edittext_isbn);
        mSearchButton = (Button) findViewById(R.id.button_searchByIsbn);
        mResultTitleTextView = (TextView) findViewById(R.id.textView_result_isbnsearch_title);
        mResultAuthorTextView = (TextView) findViewById(R.id.textView_result_isbnsearch_author);
        mAddResultButton = (Button) findViewById(R.id.button_add_isbnsearch);
        mBookReadSwitch = findViewById(R.id.switch_book_read_isbn_search);
        mProgressBar = findViewById(R.id.progress_bar_isbn_search);

        // if called with input from BarcodeScannerActivity
        if (mIsbn != null) {
            mIsbnEditText.setText(mIsbn);
        }

        // hide ui elements that display results
        mBookReadSwitch.setVisibility(View.INVISIBLE);
        mAddResultButton.setVisibility(View.INVISIBLE);
    }

    private void startDownload() {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.setDownloadUrl(getQuery());
            // Execute the async download.
            mDownloading = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mNetworkFragment.startDownload();
        }
    }

    private String getQuery() {
        String mIsbnQueryInput = mIsbnEditText.getText().toString();
        mIsbn = mIsbnQueryInput;
        return BOOK_BASE_URL + QUERY_PARAM_ISBN + mIsbnQueryInput;
    }

    private void clearResults() {
        mTitle = null;
        mAuthor = null;
        mResultTitleTextView.setText(null);
        mResultAuthorTextView.setText(null);
        mBookReadSwitch.setVisibility(View.INVISIBLE);
        mAddResultButton.setVisibility(View.INVISIBLE);
    }
    ///////////////////////////////////////// download in progress or finished

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void updateFromDownload(Object result) {
        if (result == null) {
            mDownloading = false;
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(IsbnSearchActivity.this, "Error: Download Failed!", Toast.LENGTH_LONG).show();
            return;
        }
        if (result instanceof Exception) {
            Toast.makeText(IsbnSearchActivity.this, result.toString(), Toast.LENGTH_LONG).show();
        } else {
            // Update UI based on result of download
            if (getResults(result.toString())) {
                if (mSubtitle.length() == 0) {
                    mResultTitleTextView.setText(mTitle);
                } else if (mTitle.length() == 0) {
                    mResultTitleTextView.setText(mSubtitle);
                } else {
                    String concatString = mTitle + " - " + mSubtitle;
                    mResultTitleTextView.setText(concatString);
                }
                mResultAuthorTextView.setText(mAuthor);
                mBookReadSwitch.setVisibility(View.VISIBLE);
                mAddResultButton.setVisibility(View.VISIBLE);
            }
        }
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
        mProgressBar.setVisibility(View.GONE);
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }

    private boolean getResults(String resultString) {
        // parse JSON - results from Google Books API
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

                try {
                    mSubtitle = volumeInfo.getString("subtitle");
                } catch (JSONException e) {
                    e.printStackTrace();
                    mSubtitle = "";
                }

                try {
                    mPublisher = volumeInfo.getString("publisher");
                } catch (JSONException e) {
                    e.printStackTrace();
                    mPublisher = "";
                }
                try {
                    mPublishedYear = volumeInfo.getString("publishedDate").substring(0, 4);
                } catch (JSONException e) {
                    e.printStackTrace();
                    mPublishedYear = "";
                }
                try {
                    mPages = volumeInfo.getInt("pageCount");
                } catch (JSONException e) {
                    e.printStackTrace();
                    mPages = 0;
                }
                try {
                    mDescription = volumeInfo.getString("description");
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDescription = "";
                }
                try {
                    JSONObject jsonImage = volumeInfo.getJSONObject("imageLinks");
                    mCoverUrl = jsonImage.getString("thumbnail");
                } catch (JSONException e) {
                    e.printStackTrace();
                    mCoverUrl = "";
                }
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            // Result was Exception
            Toast.makeText(IsbnSearchActivity.this, "No Book found!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private String cleanAuthorString(String string) {
        string = string.substring(1, string.length() - 1);
        String[] strings = string.split("\"");
        String res = strings[0];
        for (int i = 1; i < strings.length; i++) {
            res += strings[i];
        }
        res = res.trim();
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

    private void uploadBookData() {
        DatabaseReference nextBookDatabaseReference = mBookListDatabaseReference.push();
        Book bookItem = new Book(nextBookDatabaseReference.getKey(), mBookWasRead, mCoverUrl, mTitle, mSubtitle, mAuthor, mIsbn, mPublisher, mPublishedYear, mPages, mDescription);
        nextBookDatabaseReference.setValue(bookItem);

        DatabaseService.getInstance().getBookService().incrementTotalNumOfBooks();
        if (mBookWasRead) {
            DatabaseService.getInstance().getAchievementService().incrementNumOfReadBooks();
        }
    }
}
