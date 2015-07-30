package com.antoshkaplus.words.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 7/28/15.
 */
@DatabaseTable(tableName = UserTranslation.TABLE_NAME)
public class UserTranslation {

    public static final String TABLE_NAME = "user_translation";

    public static final String FIELD_USER = "user";
    public static final String FIELD_TRANSLATION = "translation";

    @DatabaseField(columnName = FIELD_USER,
                    foreign = true)
    public User user;

    @DatabaseField(columnName = FIELD_TRANSLATION,
                    foreign = true,
                    foreignAutoRefresh = true,
                    maxForeignAutoRefreshLevel = 3)
    public Translation translation;


    public UserTranslation() {}

    public UserTranslation(User user, Translation translation) {
        this.user = user;
        this.translation = translation;
    }
}
