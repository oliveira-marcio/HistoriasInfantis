package com.abobrinha.caixinha.data;

public class Paragraph {
    private int mType;
    private String mContent;

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUTHOR = 2;
    public static final int TYPE_END = 3;

    public static final String AUTHOR = "Rodrigo Lopes";
    public static final String END = "FIM";

    public Paragraph() {
    }

    public void setType(int type) {
        mType = type;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public int getType() {
        return mType;
    }

    public String getContent() {
        return mContent;
    }
}
