package com.example.fzeih.bookshelf;

import com.google.firebase.database.DatabaseReference;

public interface BookDeletionListener {

    void bookDeleted(DatabaseReference deletedBookDatabaseReference, Book deletedBook);
}
