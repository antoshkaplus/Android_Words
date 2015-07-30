package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = NativeWord.TABLE_NAME)
public class NativeWord extends Word {
    public static final String TABLE_NAME = "native_word";

    public NativeWord() {}

    public NativeWord(String word) {
        super(word);
    }

}
