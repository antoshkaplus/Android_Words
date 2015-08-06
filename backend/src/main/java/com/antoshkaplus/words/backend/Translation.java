package com.antoshkaplus.words.backend;

/**
 * Created by antoshkaplus on 8/5/15.
 */
public class Translation {
    private String foreignWord;
    private String nativeWord;


    public String getNativeWord() {
        return nativeWord;
    }

    public void setNativeWord(String nativeWord) {
        this.nativeWord = nativeWord;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public void setForeignWord(String foreignWord) {
        this.foreignWord = foreignWord;
    }

}
