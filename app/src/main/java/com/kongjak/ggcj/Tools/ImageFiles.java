package com.kongjak.ggcj.Tools;

import android.graphics.drawable.Drawable;

public class ImageFiles {
    public String title;
    public String url;
    public boolean isImageAvailable;
    public Drawable image;

    public ImageFiles(String title, String url,boolean isImageAvailable, Drawable image) {
        this.title = title;
        this.url = url;
        this.isImageAvailable = isImageAvailable;
        this.image = image;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean getImageAvailable() {
        return this.isImageAvailable;
    }

}
