package com.kongjak.ggcj.Tools;

public class Notices {
    public String title;
    public String writer;
    public String date;
    public String url;

    public Notices(String title, String writer, String date, String url) {
        this.title = title;
        this.writer = writer;
        this.date = date;
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
