package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 7/26/16.
 */
@DatabaseTable(tableName = Stats.TABLE_NAME)
public class Stats {

    public static final String TABLE_NAME = "stats";

    public static final String FIELD_FOREIGN_WORD = "foreign_word";
    public static final String FIELD_LOCAL_SCORE = "local_score";
    public static final String FIELD_SERVER_SCORE = "server_score";


    @DatabaseField(columnName = FIELD_FOREIGN_WORD, id = true)
    public String foreignWord;

    // on sync will reset server score and zero local one

    // local changes (unsync)
    @DatabaseField(columnName = FIELD_LOCAL_SCORE, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    public Score localScore;

    // server values
    @DatabaseField(columnName = FIELD_SERVER_SCORE, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    public Score serverScore;


    public Stats() {}

    public Stats(String foreignWord) {
        this.foreignWord = foreignWord;
        localScore = new Score();
        serverScore = new Score();
    }

    public int getSuccessScore() {
        return localScore.success + serverScore.success;
    }

    public int getFailureScore() {
        return localScore.failure + serverScore.failure;
    }

}
