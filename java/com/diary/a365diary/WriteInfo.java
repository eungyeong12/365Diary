package com.diary.a365diary;

import java.io.Serializable;
import java.util.Date;

public class WriteInfo implements Serializable {
    private String title;
    private String contents;
    private String userId;
    public String image;
    public Date createAt;

    private Date myDate;


    public WriteInfo() {

    }

    public WriteInfo(String title, String contents, String userId, String image, Date createAt, Date myDate) {
        this.title = title;
        this.contents = contents;
        this.userId = userId;
        this.image = image;
        this.createAt = createAt;
        this.myDate = myDate;
    }

    public WriteInfo(String title, String contents, String image, Date createAt, Date myDate) {
        this.title = title;
        this.contents = contents;
        this.image = image;
        this.createAt = createAt;
        this.myDate = myDate;
    }

    public WriteInfo(String title, String contents, Date createAt, Date myDate) {
        this.title = title;
        this.contents = contents;
        this.createAt = createAt;
        this.myDate = myDate;
    }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }
    public String getContents() { return this.contents; }
    public void setContents(String contents) { this.contents = contents; }
    public String getUserId() { return this.userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getImage() { return this.image; }
    public void setImage(String image) { this.image = image; }
    public Date getCreateAt() { return this.createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date setMyDate(Date myDate) { return this.myDate = myDate; }
    public Date getMyDate() { return this.myDate = myDate; }
}
