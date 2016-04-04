package com.antoshkaplus.words.model;

/**
 * Created by antoshkaplus on 1/27/16.
 */
public class TranslationKey {

    public String foreignWord;
    public String nativeWord;

    public TranslationKey() {}

    public TranslationKey(String foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
    }

}
