package com.example.bi1;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String bookId;
    private String bookName;
    private double unitPrice;
    private int quantity;
    private String image;
    private String description;
    private String author;
    private String publisher;
    private String year;
    private String language;

    public CartItem() {}

    public CartItem(String bookId, String bookName, double unitPrice, int quantity, String image, String description, String author, String publisher, String year, String language) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.image = image;
        this.description = description;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.language = language;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public double getTotalPrice() {
        return unitPrice * quantity;
    }
}
