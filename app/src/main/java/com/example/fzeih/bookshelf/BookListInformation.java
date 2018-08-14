package com.example.fzeih.bookshelf;

public class BookListInformation {
    private String mBookListKey;
    private String mBookListName;

    public BookListInformation(String bookListKey, String bookListName) {
        mBookListKey = bookListKey;
        mBookListName = bookListName;
    }

    public String getBookListKey() {
        return mBookListKey;
    }

    public String getBookListName() {
        return mBookListName;
    }

    public void setBookListName(String mBookListName) {
        this.mBookListName = mBookListName;
    }
}
