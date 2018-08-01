package com.example.fzeih.bookshelf;

public class Book {

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
}
