package com.example.fzeih.bookshelf.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.adapter.AchievementAdapter;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.datastructures.Achievement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

        /*
        reference to picture loading tool (Picasso):
        Square Open Source. http://square.github.io/picasso/
         */

public class ProfileActivity extends AppCompatActivity {
    private BroadcastReceiver mTotalNumOfBooksChangedBroadcastReceiver;
    private BroadcastReceiver mNumOfReadBooksChangedBroadcastReceiver;

    private FirebaseUser mUser;

    private AchievementAdapter mAchievementAdapter;

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
        getSupportActionBar().setTitle(R.string.title_activity_user_profile);
        getSupportActionBar().setElevation(0);

        // data
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        initViews();
        setUserName();
        setUserPhoto();
        setTotalNumOfBooks();
        setNumOfReadBooks();
        setAchievementAdapter();

        // Broadcast Receiver
        initTotalNumOfBooksChangedBroadcastReceiver();
        initNumOfReadBooksChangedBroadcastReceiver();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTotalNumOfBooksChangedBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNumOfReadBooksChangedBroadcastReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTotalNumOfBooksChangedBroadcastReceiver, new IntentFilter(Constants.event_totalNumOfBooks_changed));
        LocalBroadcastManager.getInstance(this).registerReceiver(mNumOfReadBooksChangedBroadcastReceiver, new IntentFilter(Constants.event_numOfReadBooks_changed));
    }

    private void initTotalNumOfBooksChangedBroadcastReceiver() {
        mTotalNumOfBooksChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Long totalNumOfBooks = extras.getLong(Constants.key_intent_totalNumOfBooks);
                    String string = "You have " + Long.toString(totalNumOfBooks) + " books.";
                    mTotalNumOfBooksTextView.setText(string);
                }
            }
        };
    }

    private void initNumOfReadBooksChangedBroadcastReceiver() {
        mNumOfReadBooksChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Long numOfReadBooks = extras.getLong(Constants.key_intent_numOfReadBooks);
                    String string = "You read " + Long.toString(numOfReadBooks) + " books.";
                    mNumOfReadBooksTextView.setText(string);
                }
                /*
                A change in numOfReadBooks could also mean that the achievements have changed.
                If so, this was already done in the AchievementService and thanks to call by reference
                we only need to check for a change here.
                 */
                mAchievementAdapter.notifyDataSetChanged();
            }
        };
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

    private void setUserPhoto() {
        if (mUser != null) {
            Uri userPhotoUrl = mUser.getPhotoUrl();
            if (userPhotoUrl != null) {
                Picasso.get().load(userPhotoUrl).placeholder(R.drawable.ic_launcher_foreground).into(mUserPhotoImageView);
            }
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
        ArrayList<Achievement> achievements = DatabaseService.getInstance().getAchievementService().getAchievementList();
        mAchievementAdapter = new AchievementAdapter(this, R.layout.achievement, achievements);
        mAchievementListView.setAdapter(mAchievementAdapter);
    }
}
