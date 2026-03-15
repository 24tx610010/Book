package com.example.bi1;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    private String id;
    private String bookId;
    private String userPhone;
    private String userName;
    private float ratingValue;
    private String comment;
    private Date timestamp;

    public Review() {}

    public Review(String id, String bookId, String userPhone, String userName, float ratingValue, String comment, Date timestamp) {
        this.id = id;
        this.bookId = bookId;
        this.userPhone = userPhone;
        this.userName = userName;
        this.ratingValue = ratingValue;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public float getRatingValue() { return ratingValue; }
    public void setRatingValue(float ratingValue) { this.ratingValue = ratingValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
