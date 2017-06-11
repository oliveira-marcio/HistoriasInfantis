package com.abobrinha.caixinha.data;

public class History {
    private String mTitle;
    private String mUrlHistory;
    private String mUrlImage;
    private String mContent;

    public History(String titulo, String urlHistory, String urlImage, String content) {
        mTitle = titulo;
        mUrlHistory = urlHistory;
        mUrlImage = urlImage;
        mContent = content;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrlHistory() {
        return mUrlHistory;
    }

    public String getUrlImage() {
        return mUrlImage;
    }

    public String getContent() {
        return mContent;
    }
}
