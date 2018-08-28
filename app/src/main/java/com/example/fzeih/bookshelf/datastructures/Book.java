package com.example.fzeih.bookshelf.datastructures;

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
    private String subtitle;
    private String author;
    private String isbn;
    private String publisher;
    private String publishedYear;
    private int pages;
    private String bookDescription;

    public Book() {
    }

    public Book(String key, boolean read, String coverUrl, String title, String subtitle, String author, String isbn,
                String publisher, String publishedYear, int pages, String bookDescription) {
        this.key = key;
        this.read = read;
        this.coverUrl = coverUrl;
        this.title = title;
        this.subtitle = subtitle;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publishedYear = publishedYear;
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Exclude
    public String getTitleWithSubtitle() {
        if (subtitle.length() == 0) {
            return title;
        } else if (title.length() == 0) {
            return subtitle;
        } else {
            return title + " - " + subtitle;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(String publishedYear) {
        this.publishedYear = publishedYear;
    }

    @Exclude
    public String getPublisherWithPublishedYear() {
        if (publisher.length() == 0) {
            return publishedYear;
        } else if (publishedYear.length() == 0) {
            return publisher;
        } else {
            return publisher + ", " + publishedYear;
        }
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
        dest.writeString(this.subtitle);
        dest.writeString(this.author);
        dest.writeString(this.isbn);
        dest.writeString(this.publisher);
        dest.writeString(this.publishedYear);
        dest.writeInt(this.pages);
        dest.writeString(this.bookDescription);
    }

    protected Book(Parcel in) {
        this.key = in.readString();
        this.read = in.readByte() != 0;
        this.coverUrl = in.readString();
        this.title = in.readString();
        this.subtitle = in.readString();
        this.author = in.readString();
        this.isbn = in.readString();
        this.publisher = in.readString();
        this.publishedYear = in.readString();
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
