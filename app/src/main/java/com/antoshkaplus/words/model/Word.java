package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * Created by antoshkaplus on 7/28/15.
 */

public abstract class Word {

    public static final String FIELD_NAME_WORD = "word";
    public static final String FIELD_NAME_ID = "id";

    @DatabaseField(columnName = FIELD_NAME_WORD, unique = true)
    public String word;

    // id field is long because we may wanna use backend where it's most certainly is going to be long
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    public long id;


    public Word() {}

    public Word(String word) {
        this.word = word;
    }
}
