package com.example.fzeih.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Book implements Parcelable {

    private String key;
    private boolean read = false;
    private String coverUrl;
    private String title;
    private String authorName;
    private String isbn;
    private String publisher;
    private String publishedDate;
    private int pages;
    private String bookDescription;

    public Book() {
    }

    public Book(String key, boolean read, String coverUrl, String title, String author, String isbn,
                String publisher, String publishedDate, int pages, String bookDescription){
        this.key = key;
        this.read = read;
        this.coverUrl = coverUrl;
        this.title = title;
        this.authorName = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.pages = pages;
        this.bookDescription = bookDescription;
    }

    public String getKey() {
        return key;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    @Exclude
    public String getPuplisherWithPublishedDate() {
        return publisher + ", " + publishedDate;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeByte(this.read ? (byte) 1 : (byte) 0);
        dest.writeString(this.coverUrl);
        dest.writeString(this.title);
        dest.writeString(this.authorName);
        dest.writeString(this.isbn);
        dest.writeString(this.publisher);
        dest.writeString(this.publishedDate);
        dest.writeInt(this.pages);
        dest.writeString(this.bookDescription);
    }

    protected Book(Parcel in) {
        this.key = in.readString();
        this.read = in.readByte() != 0;
        this.coverUrl = in.readString();
        this.title = in.readString();
        this.authorName = in.readString();
        this.isbn = in.readString();
        this.publisher = in.readString();
        this.publishedDate = in.readString();
        this.pages = in.readInt();
        this.bookDescription = in.readString();
    }

    @Exclude
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
