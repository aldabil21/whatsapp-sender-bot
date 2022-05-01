package com.wabot.model;

public class Job {
    private int id;
    private String date;

    public Job() {
        this(0, null);
    }

    public Job(int id, String date) {
        this.id = id;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public Job setId(int id) {
        this.id = id;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Job setDate(String date) {
        this.date = date;
        return this;
    }
}
