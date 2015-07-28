package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 7/28/15.
 */
@DatabaseTable(tableName = Translation.TABLE_NAME)
public class Translation {

    public static final String TABLE_NAME = "translation";

    public static final String FIELD_FOREIGN_WORD = "foreign_word";
    public static final String FIELD_NATIVE_WORD = "native_word";

    @DatabaseField(columnName = FIELD_FOREIGN_WORD)
    ForeignWord foreignWord;
    @DatabaseField(columnName = FIELD_NATIVE_WORD)
    NativeWord nativeWord;

}
