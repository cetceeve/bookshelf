package com.example.fzeih.bookshelf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.URL;

public class IsbnSearchActivity extends AppCompatActivity implements DownloadCallback {

    private EditText isbnEditText;
    private Button searchButton;
    private ListView resultListView; // TODO - ersetzen durch Ansicht eines einzelnen Buches

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    //Paramater for URL
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    private static final String QUERY_PARAM_ISBN = "q=isbn:";
    private String isbnQueryInput;

    private String requestURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn_search);

        getSupportActionBar().setTitle("ISBN Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initUI();

        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager());


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isbnQueryInput = isbnEditText.getText().toString();
                requestURL = BOOK_BASE_URL + QUERY_PARAM_ISBN + isbnQueryInput;

                startDownload();

            }
        });


    }

    private void initUI() {
        isbnEditText = (EditText) findViewById(R.id.edittext_isbn);
        searchButton = (Button) findViewById(R.id.button_searchByIsbn);
        resultListView = (ListView) findViewById(R.id.listview_isbnResultList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void updateFromDownload(Object result) {
        // Update your UI here based on result of download
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
            mNetworkFragment.startDownload(requestURL);
            mDownloading = true;
        }
    }
}
