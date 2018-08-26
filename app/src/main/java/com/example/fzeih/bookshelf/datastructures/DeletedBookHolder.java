package com.example.fzeih.bookshelf.datastructures;

import com.google.firebase.database.DatabaseReference;

public class DeletedBookHolder {
    private static Book DELETED_BOOK;
    private static DatabaseReference DELETED_BOOK_DATABASE_REFERENCE;

    public DeletedBookHolder() {}

    public static Book getDeletedBook() {
        Book deletedBook = DELETED_BOOK;
        DELETED_BOOK = null;
        return deletedBook;
    }

    public static void setDeletedBook(Book deletedBook) {
        DELETED_BOOK = deletedBook;
    }

    public static DatabaseReference getDeletedBookDatabaseReference() {
        DatabaseReference deletedBookDatabaseReference = DELETED_BOOK_DATABASE_REFERENCE;
        DELETED_BOOK_DATABASE_REFERENCE = null;
        return deletedBookDatabaseReference;
    }

    public static void setDeletedBookDatabaseReference(DatabaseReference deletedBookDatabaseReference) {
        DELETED_BOOK_DATABASE_REFERENCE = deletedBookDatabaseReference;
    }
}
