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
}
