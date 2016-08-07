package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by antoshkaplus on 7/26/16.
 *
 * we have to create separate class and put everything to Stats because there
 * may be more fields coming and we may need to expand this class even farther
 * even have another foreign field in this one
 *
 * and we can't store it as Serializable as it would hurt migration process
 */
@DatabaseTable(tableName = Score.TABLE_NAME)
public class Score {

    public static final String TABLE_NAME = "score";

    public static final String FIELD_ID = "id";
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_FAILURE = "failure";



    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    public long id;

    @DatabaseField(columnName = FIELD_SUCCESS)
    public int success;
    @DatabaseField(columnName = FIELD_FAILURE)
    public int failure;

    // intentially initialize with zeros
    public Score() {
        id = 0;
        reset();
    }

    public void reset() {
        success = 0;
        failure = 0;
    }

    public void moveTo(Score s) {
        s.success = success;
        s.failure = failure;
        reset();
    }


}
