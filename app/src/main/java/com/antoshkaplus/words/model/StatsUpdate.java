package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 7/31/16.
 *
 * we create different class/table for update because update stuff
 * is usually should be different from tracking structure. and we don't want to mix both.
 * also we may pick completely different update approach in the future.
 */
@DatabaseTable(tableName = StatsUpdate.TABLE_NAME)
public class StatsUpdate {

    public static final String TABLE_NAME = "StatsUpdate";

    @DatabaseField(canBeNull = false, id = true)
    public String foreignWord;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    public Score localScore;

    public StatsUpdate() {}

    public StatsUpdate(String foreignWord, Score localScore) {
        this.foreignWord = foreignWord;
        this.localScore = localScore;
    }

}
