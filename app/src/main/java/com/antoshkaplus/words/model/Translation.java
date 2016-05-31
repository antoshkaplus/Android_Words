package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by antoshkaplus on 7/28/15.
 */
@DatabaseTable(tableName = Translation.TABLE_NAME)
public class Translation {

    public static final String TABLE_NAME = "translation";

    public static final String FIELD_FOREIGN_WORD = "foreign_word";
    public static final String FIELD_NATIVE_WORD = "native_word";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME_CREATION_DATE = "creation_date";
    public static final String FIELD_NAME_UPDATE_DATE = "update_date";
    public static final String FIELD_NAME_DELETED = "deleted";
    public static final String FIELD_NAME_SYNCED = "synced";


    @DatabaseField(columnName = FIELD_FOREIGN_WORD,
                    index = true,
                    uniqueCombo = true)
    public String foreignWord;
    @DatabaseField(columnName = FIELD_NATIVE_WORD,
                    index = true,
                    uniqueCombo = true)
    public String nativeWord;

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    public long id;

    @DatabaseField(columnName = FIELD_NAME_CREATION_DATE,
                    canBeNull = false)
    public Date creationDate;

    @DatabaseField(columnName = FIELD_NAME_UPDATE_DATE,
                    canBeNull = false)
    public Date updateDate;

    @DatabaseField(columnName = FIELD_NAME_DELETED,
                    canBeNull = false)
    public boolean deleted;

    // synced with remote server or not
    // with this flag don't need to worry about
    // updated timestamps and sync timestamp collisions
    @DatabaseField(columnName = FIELD_NAME_SYNCED,
                    canBeNull = false,
                    index = true)
    public boolean synced;


    public Translation() {}

    // you don't create constructor that takes only two strings because
    // those classes can be extended in future
    public Translation(String foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        this.creationDate = new Date();
        this.updateDate = new Date();
        deleted = false;
        synced = false;
    }
    // not really good solution
    public Translation(String foreignWord, String nativeWord, Date creationDate) {
        this(foreignWord, nativeWord);
        this.creationDate = creationDate;
    }



}
