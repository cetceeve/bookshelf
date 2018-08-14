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

    public AchievementService getAchievementService() {
        return achievementService;
    }

    static class AchievementService {
        private DatabaseReference mNumOfReadBooksDatabaseReference;
        private ValueEventListener mNumOfReadBooksValueEventListener;
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
                    Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceListener.class);
                    for (Object listener : listeners) {
                        ((AchievementServiceListener) listener).onNumOfReadBooksChance(mNumOfReadBooks.intValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mNumOfReadBooksDatabaseReference.addValueEventListener(mNumOfReadBooksValueEventListener);
        }

        public int getNumOfReadBooks() {
            if (mNumOfReadBooks != null) {
                return mNumOfReadBooks.intValue();
            }
            return -1;
        }

        public ArrayList<Achievement> getAchievementList(Context context) {
            ArrayList<Achievement> achievements = new ArrayList<>();
            achievements.add(new Achievement(context, 10, R.string.achievement_10, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 25, R.string.achievement_25, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 50, R.string.achievement_50, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 100, R.string.achievement_100, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 250, R.string.achievement_250, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 500, R.string.achievement_500, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 750, R.string.achievement_750, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 1000, R.string.achievement_1000, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));
            achievements.add(new Achievement(context, 5000, R.string.achievement_5000, R.mipmap.ic_launcher_round, R.drawable.ic_barcode_scan));

            if (mNumOfReadBooks != null) {
                for (Achievement achievement : achievements) {
                    if (achievement.getLevel() <= mNumOfReadBooks.intValue()) {
                        achievement.toggleColored();
                    }
                }
            }
            return achievements;
        }

    }
}
