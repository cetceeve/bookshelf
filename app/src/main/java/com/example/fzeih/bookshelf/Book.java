package com.example.fzeih.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{

    private String authorName;
    private String title;
    private String isbn;
    // cover, genre, ...

    public Book() {
    }

    public Book(String authorName, String title, String isbn){
        this.authorName = authorName;
        this.title = title;
        this.isbn = isbn;
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


    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    protected Book(Parcel in) {
        authorName = in.readString();
        title = in.readString();
        isbn = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authorName);
        dest.writeString(title);
        dest.writeString(isbn);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
