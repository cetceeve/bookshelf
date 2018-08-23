package com.example.fzeih.bookshelf.listener;

import com.example.fzeih.bookshelf.datastructures.Book;
import com.google.firebase.database.DatabaseReference;

public interface BookDeletionCallback {

    void bookDeleted(DatabaseReference deletedBookDatabaseReference, Book deletedBook);
}
