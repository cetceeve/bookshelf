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
    private static boolean isStarted = false;
    private static AchievementService achievementService;
    private static BookService bookService;

    static DatabaseService getInstance() {
        return ourInstance;
    }

    private DatabaseService() {
    }

    public void startServices(Context context) {
        if (!isStarted) {
            achievementService = new AchievementService();
            achievementService.getAchievementList(context);
            bookService = new BookService();
            isStarted = true;
        }
    }

    public AchievementService getAchievementService() {
        return achievementService;
    }

    public BookService getBookService() {
        return bookService;
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
                        ((AchievementServiceListener) listener).onNumOfReadBooksChanged(mNumOfReadBooks);
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
                Object[] listeners = ListenerAdministrator.getInstance().getListener(AchievementServiceListener.class);
                for (Object listener : listeners) {
                    ((AchievementServiceListener) listener).onAchievementChanged(highestAchievement);
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


    static class BookService {
        private DatabaseReference mTotalNumOfBooksDatabaseReference;
        private ValueEventListener mTotalNumOfBooksValueEventListener;
        private Long mTotalNumOfBooks;

        private BookService() {
            getDatabaseReference();
            attachTotalNumOfBooksDatabaseReadListener();
        }

        private void getDatabaseReference() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                mTotalNumOfBooksDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_books_total);
            } else {
                System.out.println("ERROR: no firebase user");
            }
        }

        private void attachTotalNumOfBooksDatabaseReadListener() {
            mTotalNumOfBooksValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mTotalNumOfBooks = (Long) dataSnapshot.getValue();
                    if (mTotalNumOfBooks == null) {
                        mTotalNumOfBooks = 0L;
                    }

                    Object[] listeners = ListenerAdministrator.getInstance().getListener(BookServiceListener.class);
                    for (Object listener : listeners) {
                        ((BookServiceListener) listener).onTotalNumOfBooksChanged(mTotalNumOfBooks);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mTotalNumOfBooksDatabaseReference.addValueEventListener(mTotalNumOfBooksValueEventListener);
        }

        //////////////////////////////////////////////////////////////////
        // Services

        @NonNull
        public Long getTotalNumOfBooks() {
            if (mTotalNumOfBooks != null) {
                return mTotalNumOfBooks;
            }
            return 0L;
        }

        public void incrementTotalNumOfBooks() {
            mTotalNumOfBooksDatabaseReference.setValue(mTotalNumOfBooks + 1);
        }

        public void decrementTotalNumOfBooks() {
            mTotalNumOfBooksDatabaseReference.setValue(mTotalNumOfBooks - 1);
        }

        public void decrementTotalNumOfBooks(int amount) {
            mTotalNumOfBooksDatabaseReference.setValue(mTotalNumOfBooks.intValue() - amount);
        }
    }
}
