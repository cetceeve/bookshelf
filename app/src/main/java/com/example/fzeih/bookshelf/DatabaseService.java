package com.example.fzeih.bookshelf;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

class DatabaseService {
    private static final DatabaseService ourInstance = new DatabaseService();
    private static AchievementService achievementService;

    static DatabaseService getInstance() {
        return ourInstance;
    }

    private DatabaseService() {
        achievementService = new AchievementService();
    }

    public void startServices(Context context) {
        achievementService.getAchievementList(context);
    }

    public AchievementService getAchievementService() {
        return achievementService;
    }

    static class AchievementService {
        private DatabaseReference mNumOfReadBooksDatabaseReference;
        private ValueEventListener mNumOfReadBooksValueEventListener;
        private ArrayList<Achievement> mAchievements;
        private Long mNumOfReadBooks;

        private AchievementService() {
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

                    Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceListener.class);
                    for (Object listener : listeners) {
                        ((AchievementServiceListener) listener).onNumOfReadBooksChance(mNumOfReadBooks);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mNumOfReadBooksDatabaseReference.addValueEventListener(mNumOfReadBooksValueEventListener);
        }

        private void checkForAchievementChange() {
            boolean achievementChanged = false;
            Achievement highestAchievement = null;
            if (mAchievements != null && mNumOfReadBooks != null) {
                for (Achievement achievement : mAchievements) {
                    if (achievement.getLevel() <= mNumOfReadBooks.intValue()) {
                        boolean didChange = achievement.setColored();
                        achievementChanged = achievementChanged || didChange;
                        highestAchievement = achievement;
                    } else {
                        boolean didChange = achievement.removeColored();
                        achievementChanged = achievementChanged || didChange;
                    }
                }
            }

            if (achievementChanged && highestAchievement != null) {
                Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceListener.class);
                for (Object listener : listeners) {
                    ((AchievementServiceListener) listener).onAchievementChanged(highestAchievement);
                }
            }
        }

        //////////////////////////////////////////////////////////////////
        // Services

        public Long getNumOfReadBooks() {
            if (mNumOfReadBooks != null) {
                return mNumOfReadBooks;
            }
            return 0L;
        }

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

        public void incrementNumOfReadBooks() {
            mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks + 1);
        }

        public void decrementNumOfReadBooks() {
            mNumOfReadBooksDatabaseReference.setValue(mNumOfReadBooks - 1);
        }
    }
}
