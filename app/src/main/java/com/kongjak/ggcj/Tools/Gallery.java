package com.kongjak.ggcj.Tools;

import android.graphics.drawable.Drawable;

public class Gallery {
    public String title;
    public String url;
    public Drawable thumbnail;

    public Gallery(String title, String url, Drawable thumbnail) {
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return this.url;
    }
}
