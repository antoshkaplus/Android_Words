package com.antoshkaplus.words;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telecom.Call;

import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordScore;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordScoreList;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordStats;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordStatsList;
import com.antoshkaplus.words.model.StatsUpdate;
import com.antoshkaplus.words.model.Translation;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 8/6/16.
 *
 * maybe rename to suffix task
 */
public class StatsUpdateBackend implements Callable<SyncResult> {

    TranslationRepository repo;
    Context ctx;
    String account;

    public StatsUpdateBackend(Context context, String account) {
        ctx = context;
        repo = new TranslationRepository(context);
        this.account = account;
    }

    String generateUUID() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        String uuid = UUID.randomUUID().toString();
        editor.putString(StatsUpdate.TABLE_NAME, uuid);
        editor.commit();
        return uuid;
    }

    String retrieveUUID() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(StatsUpdate.TABLE_NAME, null);
    }

    void resetUUID() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(StatsUpdate.TABLE_NAME);
        editor.commit();
    }

    public SyncResult call() {
        SyncResult r = SyncResult.SUCCESS;
        try {
            List<StatsUpdate> updates = repo.getStatsUpdates();
            String uuid;
            if (updates.size() != 0) {
                // there are some updates that weren't probably sent to server
                uuid = retrieveUUID();
                if (uuid == null) {
                    uuid = generateUUID();
                }
            } else {
                uuid = generateUUID();
                repo.prepareStatsUpdates();
                updates = repo.getStatsUpdates();
            }

            GoogleAccountCredential credential = CredentialFactory.create(ctx, account);

            DictionaryApi.Builder builder = new DictionaryApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(),
                    credential);

            builder.setRootUrl(BuildConfig.HOST);
            builder.setApplicationName("antoshkaplus-words");
            final DictionaryApi api = builder.build();

            List<ForeignWordStats> list = new ArrayList<>(updates.size());
            for (StatsUpdate u : updates) {
                ForeignWordStats s = new ForeignWordStats();
                s.setForeignWord(u.foreignWord);
                s.setFailureScore(u.localScore.failure);
                s.setSuccessScore(u.localScore.success);
                list.add(s);
            }
            ForeignWordStatsList backendList = new ForeignWordStatsList();
            backendList.setList(list);

            api.updateForeignWordStats(uuid, backendList).execute();

            repo.resetStatsUpdates();

        } catch (Exception ex) {
            ex.printStackTrace();
            r = SyncResult.FAILURE_UNKNOWN;
            // TODO
        }
        return r;
    }
}
