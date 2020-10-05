package com.emekachukwulobe.cruddemo2;

import java.util.Date;

public class Note {
    private String title;
    private String description;
    private int priority;

    private Date date;

    public Note() {
    }

    public Note(String title, String description, int priority, Date date) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
