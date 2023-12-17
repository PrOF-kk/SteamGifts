package net.mabako.steamgifts.data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Image implements Serializable {
    private String url, title;

    public Image(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("(%s,%s)", url, title);
    }
}
