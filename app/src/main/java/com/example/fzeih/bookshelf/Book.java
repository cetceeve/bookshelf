package com.example.fzeih.bookshelf;

public class Book {

    private String authorLastName;
    private String authorSurName;
    private String title;
    private String isbn;
    // cover, genre, ...

    public Book(String authorLastName, String authorSurName, String title, String isbn){
        this.authorLastName = authorLastName;
        this.authorSurName = authorSurName;
        this.title = title;
        this.isbn = isbn;
    }

    // minimal input: title
    public Book(String title){
        this("", "", title, "");
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public void setAuthorSurName(String authorSurName) {
        this.authorSurName = authorSurName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public String getAuthorSurName() {
        return authorSurName;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }
}
