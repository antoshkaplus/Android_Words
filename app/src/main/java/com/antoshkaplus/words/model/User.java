package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 7/30/15.
 */
@DatabaseTable(tableName = User.TABLE_NAME)
public class User {
    public static final String TABLE_NAME = "user";

    public static final String FIELD_ID = "id";
    public static final String FIELD_ACCOUNT = "account";


    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    public long id;
    @DatabaseField(columnName = FIELD_ACCOUNT, unique = true)
    public String account;

    public User() {}

    public User(String account) {
        this.account = account;
    }
}
