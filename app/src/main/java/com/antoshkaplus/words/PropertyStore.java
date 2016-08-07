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
    private static final String STATS_LAST_UPDATE_VERSION = "statsLastUpdateVersion";


    // means that never was synchronized
    // server would have version zero
    private static final int LAST_SYNC_VERSION_INIT = -1;
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
        ed.putInt(LAST_SUCCESSFUL_UPDATE, LAST_SYNC_VERSION_INIT);
        ed.apply();
    }

    public int getLastSyncVersion() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(LAST_UPDATE_VERSION, LAST_SYNC_VERSION_INIT);
    }

    public void setLastSyncVersion(int lastSyncVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(LAST_UPDATE_VERSION, lastSyncVersion);
        ed.apply();
    }

    public int getStatsLastUpdateVersion() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(STATS_LAST_UPDATE_VERSION, LAST_SYNC_VERSION_INIT);
    }

    public void setStatsLastUpdateVersion(Integer statsLastUpdateVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(STATS_LAST_UPDATE_VERSION, statsLastUpdateVersion);
        ed.apply();
    }
}
