package com.antoshkaplus.words;

/**
 * Created by antoshkaplus on 8/11/16.
 */
public enum GuessWordGameType {
    ForeignWord,
    NativeWord;

    private GuessWordGameType another;

    static {
        ForeignWord.another = NativeWord;
        NativeWord.another = ForeignWord;
    }

    public GuessWordGameType getAnother() {
        return another;
    }

}
