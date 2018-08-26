package com.example.fzeih.bookshelf.datastructures;

public class DeletedBookHolder {
    private static Book DELETED_BOOK;

    public DeletedBookHolder() {}

    public static Book getDeletedBook() {
        Book deletedBook = DELETED_BOOK;
        DELETED_BOOK = null;
        return deletedBook;
    }

    public static void setDeletedBook(Book deletedBook) {
        DELETED_BOOK = deletedBook;
    }
}
