package com.example.fzeih.bookshelf.database_service;

import android.support.annotation.NonNull;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.listener.BookServiceCallback;
import com.example.fzeih.bookshelf.listener.ListenerAdministrator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookService {
    private DatabaseReference mTotalNumOfBooksDatabaseReference;
    private ValueEventListener mTotalNumOfBooksValueEventListener;
    private Long mTotalNumOfBooks = 0L;

    BookService() {
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

                Object[] listeners = ListenerAdministrator.getInstance().getListener(BookServiceCallback.class);
                for (Object listener : listeners) {
                    ((BookServiceCallback) listener).onTotalNumOfBooksChanged(mTotalNumOfBooks);
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