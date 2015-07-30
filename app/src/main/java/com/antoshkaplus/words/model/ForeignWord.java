package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by antoshkaplus on 7/28/15.
 */
@DatabaseTable(tableName = ForeignWord.TABLE_NAME)
public class ForeignWord extends Word {
    public static final String TABLE_NAME = "foreign_word";

    public static final String FIELD_NAME_CREATION_DATE = "creation_date";

    @DatabaseField(columnName = FIELD_NAME_CREATION_DATE)
    public Date creationDate;


    public ForeignWord() {}

    public ForeignWord(String word, Date creationDate) {
        super(word);
        this.creationDate = creationDate;
    }


}
