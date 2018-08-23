package com.example.fzeih.bookshelf.database_service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.datastructures.Achievement;
import com.example.fzeih.bookshelf.listener.AchievementServiceCallback;
import com.example.fzeih.bookshelf.listener.ListenerAdministrator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AchievementService {
    private DatabaseReference mNumOfReadBooksDatabaseReference;
    private ValueEventListener mNumOfReadBooksValueEventListener;
    private ArrayList<Achievement> mAchievements;
    private Long mNumOfReadBooks = 0L;

    AchievementService() {
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
        mNumOfReadBooksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNumOfReadBooks = (Long) dataSnapshot.getValue();
                if (mNumOfReadBooks == null) {
                    mNumOfReadBooks = 0L;
                }

                checkForAchievementChange();

                Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceCallback.class);
                for (Object listener : listeners) {
                    ((AchievementServiceCallback) listener).onNumOfReadBooksChanged(mNumOfReadBooks);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mNumOfReadBooksDatabaseReference.addValueEventListener(mNumOfReadBooksValueEventListener);
    }

    private void checkForAchievementChange() {
        Achievement highestAchievement = null;
        boolean colorAdded = false;
        boolean colorRemoved = false;
        if (mAchievements != null && mNumOfReadBooks != null) {
            for (Achievement achievement : mAchievements) {
                if (achievement.getLevel() <= mNumOfReadBooks.intValue()) {
                    colorAdded = achievement.setColored();
                    if (colorAdded) {
                        highestAchievement = achievement;
                    }
                } else {
                    colorRemoved = achievement.removeColored();
                }
            }
        }

        if (colorAdded || colorRemoved) {
            Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceCallback.class);
            for (Object listener : listeners) {
                ((AchievementServiceCallback) listener).onAchievementChanged(highestAchievement);
            }
        }
    }

    //////////////////////////////////////////////////////////////////
    // Services

    public ArrayList<Achievement> getAchievementList(Context context) {
        if (mAchievements == null) {
            mAchievements = new ArrayList<>();
            mAchievements.add(new Achievement(context, 10, R.string.achievement_10, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 25, R.string.achievement_25, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 50, R.string.achievement_50, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 100, R.string.achievement_100, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 250, R.string.achievement_250, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 500, R.string.achievement_500, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 750, R.string.achievement_750, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 1000, R.string.achievement_1000, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            mAchievements.add(new Achievement(context, 5000, R.string.achievement_5000, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
        }

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