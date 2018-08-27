package com.example.fzeih.bookshelf.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.fragments.NetworkFragment;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.adapter.AchievementAdapter;
import com.example.fzeih.bookshelf.datastructures.Achievement;
import com.example.fzeih.bookshelf.listener.AchievementServiceCallback;
import com.example.fzeih.bookshelf.listener.BookServiceCallback;
import com.example.fzeih.bookshelf.listener.DownloadCallback;
import com.example.fzeih.bookshelf.listener.ListenerAdministrator;
import com.example.fzeih.bookshelf.listener.NetworkFragmentCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements DownloadCallback, NetworkFragmentCallback, AchievementServiceCallback, BookServiceCallback {
    private FirebaseUser mUser;

    private AchievementAdapter mAchievementAdapter;

    private NetworkFragment mNetworkFragment;
    private boolean mDownloading = false;

    private ListView mAchievementListView;
    private TextView mTotalNumOfBooksTextView;
    private TextView mNumOfReadBooksTextView;
    private TextView mUserNameTextView;
    private ImageView mUserPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setElevation(0);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager());

        initViews();
        setUserName();
        setTotalNumOfBooks();
        setNumOfReadBooks();
        setAchievementAdapter();

        // register as listener
        ListenerAdministrator.getInstance().registerListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ListenerAdministrator.getInstance().removeListener(this);
    }

    private void initViews() {
        mAchievementListView = findViewById(R.id.list_view_profile_achievements);
        mUserNameTextView = findViewById(R.id.text_view_profile_user_name);
        mTotalNumOfBooksTextView = findViewById(R.id.text_view_profile_amount_books);
        mNumOfReadBooksTextView = findViewById(R.id.text_view_profile_read_books);
        mUserPhotoImageView = findViewById(R.id.image_view_profile_user_photo);
    }

    private void setUserName() {
        if (mUser != null) {
            mUserNameTextView.setText(mUser.getDisplayName());
        } else {
            Toast.makeText(ProfileActivity.this, "ERROR: user is not signed in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setTotalNumOfBooks() {
        Long totalNumOfReadBooks = DatabaseService.getInstance().getBookService().getTotalNumOfBooks();
        String string = "You have " + Long.toString(totalNumOfReadBooks) + " books.";
        mTotalNumOfBooksTextView.setText(string);
    }

    private void setNumOfReadBooks() {
        Long numOfReadBooks = DatabaseService.getInstance().getAchievementService().getNumOfReadBooks();
        String string = "You read " + Long.toString(numOfReadBooks) + " books.";
        mNumOfReadBooksTextView.setText(string);
    }

    private void setAchievementAdapter() {
        ArrayList<Achievement> achievements = DatabaseService.getInstance().getAchievementService().getAchievementList(this);
        mAchievementAdapter = new AchievementAdapter(this, R.layout.achievement, achievements);
        mAchievementListView.setAdapter(mAchievementAdapter);
    }

    @Override
    public void onTotalNumOfBooksChanged(@NonNull Long totalNumOfBooks) {
        String string = "You have " + Long.toString(totalNumOfBooks) + " books.";
        mTotalNumOfBooksTextView.setText(string);
    }

    @Override
    public void onNumOfReadBooksChanged(@NonNull Long numOfReadBooks) {
        String string = "You read " + Long.toString(numOfReadBooks) + " books.";
        mNumOfReadBooksTextView.setText(string);
    }

    @Override
    public void onAchievementChanged(Achievement highestAchievement) {
        mAchievementAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateFromDownload(Object result) {
        if (result instanceof Exception) {
            Toast.makeText(ProfileActivity.this, result.toString(), Toast.LENGTH_LONG).show();
        } else {
            mUserPhotoImageView.setImageBitmap((Bitmap) result);
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

    @Override
    public void onNetworkFragmentInitComplete() {
        setUserPhoto();
    }

    private void setUserPhoto() {
        if (mUser != null) {
            Uri userPhotoUrl = mUser.getPhotoUrl();
            if (userPhotoUrl != null) {
                startDownload(userPhotoUrl.toString());
            }
        } else {
            Toast.makeText(ProfileActivity.this, "ERROR: user is not signed in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startDownload(String url) {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.prepareDownload(url, NetworkFragment.DOWNLOAD_RESULT_TYPE.BITMAP);
            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }
}