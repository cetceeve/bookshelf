package com.example.fzeih.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    private String key;
    private String authorName;
    private String title;
    private String isbn;
    private boolean read = false;
    // cover, genre, ...

    public Book() {
    }

    public Book(String key, String authorName, String title, String isbn){
        this.key = key;
        this.authorName = authorName;
        this.title = title;
        this.isbn = isbn;
        this.read = false;
    }

    public Book(String key, String authorName, String title, String isbn, boolean read){
        this.key = key;
        this.authorName = authorName;
        this.title = title;
        this.isbn = isbn;
        this.read = read;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getKey() {
        return key;
    }


    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean getRead() {
        return read;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.authorName);
        dest.writeString(this.title);
        dest.writeString(this.isbn);
        dest.writeByte(this.read ? (byte) 1 : (byte) 0);
    }

    protected Book(Parcel in) {
        this.key = in.readString();
        this.authorName = in.readString();
        this.title = in.readString();
        this.isbn = in.readString();
        this.read = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
