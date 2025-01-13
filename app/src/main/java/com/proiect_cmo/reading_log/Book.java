package com.proiect_cmo.reading_log;

import com.google.firebase.firestore.DocumentReference;

public class Book {
    private String author;
    private String genre;
    private String name;
    private String status;
    private DocumentReference userId;

    public Book() {

    }

    public Book( String author, String genre, String name, String status, DocumentReference userId) {
        this.author = author;
        this.genre = genre;
        this.name = name;
        this.status = status;
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public
    DocumentReference getUserId() {
        return userId;
    }

    public void setUserId(
            DocumentReference userId) {
        this.userId = userId;
    }
}

