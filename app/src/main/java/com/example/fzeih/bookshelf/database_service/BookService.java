package com.example.fzeih.bookshelf.database_service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.example.fzeih.bookshelf.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookService {
    private Context mContext;
    private DatabaseReference mTotalNumOfBooksDatabaseReference;
    private Long mTotalNumOfBooks = 0L;

    BookService(Context context) {
        mContext = context;
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
        ValueEventListener mTotalNumOfBooksValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTotalNumOfBooks = (Long) dataSnapshot.getValue();
                if (mTotalNumOfBooks == null) {
                    mTotalNumOfBooks = 0L;
                }

                // send data change via local broadcast
                Intent totalNumOfBooksIntent = new Intent(Constants.event_totalNumOfBooks_changed);
                totalNumOfBooksIntent.putExtra(Constants.key_intent_totalNumOfBooks, mTotalNumOfBooks);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(totalNumOfBooksIntent);
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