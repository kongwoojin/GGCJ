package com.kongjak.ggcj.Tools;

public class Notices {
    public String title;
    public String writer;
    public String date;
    public String url;
    public boolean isImportant;

    public Notices(String title, String writer, String date, String url, boolean isImportant) {
        this.title = title;
        this.writer = writer;
        this.date = date;
        this.url = url;
        this.isImportant = isImportant;
    }

    public String getUrl() {
        return this.url;
    }
}
