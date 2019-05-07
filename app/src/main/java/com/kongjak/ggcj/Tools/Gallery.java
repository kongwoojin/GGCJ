package com.kongjak.ggcj.Tools;

import android.graphics.drawable.Drawable;

public class Gallery {
    public String title;
    public String url;
    // public Drawable thumbnail;
    public String imageUrl;

    public Gallery(String title, String url, String imageUrl) {
        this.title = title;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return this.url;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
}
