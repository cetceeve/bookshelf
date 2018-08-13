package com.example.fzeih.bookshelf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ArrayList<Achievement> achievements;
    private AchievementAdapter achievementAdapter;

    private ListView mAchievementListView;
    private TextView mNumOfBooksTextView;
    private TextView mNumOfReadBooksTextView;
    private ImageView mUserPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Name");

        initViews();
        setAdapter();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        mAchievementListView = findViewById(R.id.list_view_profile_achievements);
        mNumOfBooksTextView = findViewById(R.id.text_view_profile_amount_books);
        mNumOfReadBooksTextView = findViewById(R.id.text_view_profile_read_books);
        mUserPhotoImageView = findViewById(R.id.image_view_profile_user_photo);
    }

    private void setAdapter() {
        createAchievements();
        achievementAdapter = new AchievementAdapter(this, R.layout.achievement, achievements);
        mAchievementListView.setAdapter(achievementAdapter);
    }

    private void createAchievements() {
        achievements = new ArrayList<>();
        achievements.add(new Achievement(this,10, R.string.achievement_10, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,25, R.string.achievement_25, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,50 ,R.string.achievement_50, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,100 ,R.string.achievement_100, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,250 ,R.string.achievement_250, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,500 ,R.string.achievement_500, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,1000 ,R.string.achievement_1000, R.mipmap.ic_launcher_round));
        achievements.add(new Achievement(this,5000 ,R.string.achievement_5000, R.mipmap.ic_launcher_round));
    }
}
