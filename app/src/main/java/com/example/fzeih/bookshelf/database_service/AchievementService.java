package com.example.fzeih.bookshelf.database_service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Achievement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AchievementService {
    private Context mContext;
    private DatabaseReference mNumOfReadBooksDatabaseReference;
    private ArrayList<Achievement> mAchievements;
    private Long mNumOfReadBooks = 0L;

    AchievementService(Context context) {
        mContext = context;
        getDatabaseReference();
        attachNumOfReadBooksDatabaseReadListener();
    }

    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mNumOfReadBooksDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_books_read);
        } else {
            System.out.println("ERROR: no firebase user");
        }
    }

    private void attachNumOfReadBooksDatabaseReadListener() {
        ValueEventListener mNumOfReadBooksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNumOfReadBooks = (Long) dataSnapshot.getValue();
                if (mNumOfReadBooks == null) {
                    mNumOfReadBooks = 0L;
                }

                // send data change via local broadcast
                Intent numOfReadBooksIntent = new Intent(Constants.event_numOfReadBooks_changed);
                numOfReadBooksIntent.putExtra(Constants.key_intent_numOfReadBooks, mNumOfReadBooks);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(numOfReadBooksIntent);

                checkForAchievementChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mNumOfReadBooksDatabaseReference.addValueEventListener(mNumOfReadBooksValueEventListener);
    }

    private void checkForAchievementChange() {
        Achievement highestAchievement = null;

        if (mAchievements != null && mNumOfReadBooks != null) {
            // color all achieved achievements
            for (Achievement achievement : mAchievements) {
                if (achievement.getLevel() <= mNumOfReadBooks.intValue()) {
                    /*
                    note that achievement.setColored() returns if the color change was
                    actually necessary! This means only for the single achievement where a
                    change actually occurred this will be true.
                     */
                    if (achievement.setColored()) {
                        highestAchievement = achievement;
                    }
                } else {
                    // the user can also loose achievements (not by deleting books tho)
                    achievement.removeColored();
                }
            }
        }

        if (highestAchievement != null) {
            // send achievement text for snackbar via local broadcast
            Intent newAchievementIntent = new Intent(Constants.event_new_achievement);
            newAchievementIntent.putExtra(Constants.key_intent_achievement_text, highestAchievement.getAchievementText());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(newAchievementIntent);
        }
    }

    //////////////////////////////////////////////////////////////////
    // Services

    public ArrayList<Achievement> getAchievementList() {
        if (mAchievements == null) {
            mAchievements = new ArrayList<>();
            mAchievements.add(new Achievement(mContext, 5, R.string.achievement_10, R.drawable.achievement_colored_5, R.drawable.achievement_grey_5));
            mAchievements.add(new Achievement(mContext, 10, R.string.achievement_25, R.drawable.achievement_colored_10, R.drawable.achievement_grey_10));
            mAchievements.add(new Achievement(mContext, 15, R.string.achievement_50, R.drawable.achievement_colored_15, R.drawable.achievement_grey_15));
            mAchievements.add(new Achievement(mContext, 25, R.string.achievement_100, R.drawable.achievement_colored_25, R.drawable.achievement_grey_25));
            mAchievements.add(new Achievement(mContext, 50, R.string.achievement_250, R.drawable.achievement_colored_50, R.drawable.achievement_grey_50));
            mAchievements.add(new Achievement(mContext, 70, R.string.achievement_500, R.drawable.achievement_colored_70, R.drawable.achievement_grey_70));
            mAchievements.add(new Achievement(mContext, 90, R.string.achievement_750, R.drawable.achievement_colored_90, R.drawable.achievement_grey_90));
        }

        // color all achieved achievements
        if (mNumOfReadBooks != null) {
            for (Achievement achievement : mAchievements) {
                if (achievement.getLevel() <= mNumOfReadBooks.intValue()) {
                    achievement.setColored();
                }
            }
        }

        return mAchievements;
    }

    @NonNull
    public Long getNumOfReadBooks() {
        if (mNumOfReadBooks != null) {
            return mNumOfReadBooks;
        }
        return 0L;
    }

    public void incrementNumOfReadBooks() {
        mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks + 1);
    }

    public void decrementNumOfReadBooks() {
        mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks - 1);
    }
}