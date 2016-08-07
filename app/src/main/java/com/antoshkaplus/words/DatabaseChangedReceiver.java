package com.antoshkaplus.words;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by antoshkaplus on 7/29/16.
 */
public abstract class DatabaseChangedReceiver extends BroadcastReceiver {
    public static String ACTION_DATABASE_CHANGED = "com.antoshkaplus.words.DATABASE_CHANGED";
    private static String EXTRA_TABLE_NAMES = "TABLE_NAMES";

    public static void putTableNames(Intent intent, String[] tableNames) {
        intent.putExtra(ACTION_DATABASE_CHANGED, tableNames);
    }

    public static Set<String> getTableNames(Intent intent) {
        String[] changed = (String[]) intent.getStringArrayExtra(ACTION_DATABASE_CHANGED);
        return new HashSet<>(Arrays.asList(changed));
    }
}
