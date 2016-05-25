package com.antoshkaplus.words;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

/**
 * Created by antoshkaplus on 1/27/16.
 */
public class PropertyStore {

    private static final String LAST_UPDATE_VERSION = "lastUpdateVersion";
    private static final String LAST_SUCCESSFUL_UPDATE = "lastSuccessfulUpdate";
    private static final String FIRST_LAUNCH = "firstLaunch";
    private Context context;

    PropertyStore(Context context) {
        this.context = context;
    }

    public boolean isFirstLaunch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains(FIRST_LAUNCH);
    }

    // this is main property that controls initialization
    public void setFirstLaunch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean(FIRST_LAUNCH, true);
        ed.putLong(LAST_SUCCESSFUL_UPDATE, 0);
        ed.apply();
    }

    public Date lastSuccessfulUpdate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Date(prefs.getLong(LAST_SUCCESSFUL_UPDATE, 0));
    }

    public void setLastSuccessfulUpdate(Date date) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putLong(LAST_SUCCESSFUL_UPDATE, date.getTime());
        ed.apply();
    }

    public int lastSyncVersion() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(LAST_UPDATE_VERSION, 0);
    }

    public void setLastSyncVersion(int lastSyncVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putLong(LAST_UPDATE_VERSION, lastSyncVersion);
        ed.apply();
    }
}
