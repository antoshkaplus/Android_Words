package com.antoshkaplus.words;

import android.content.Context;
import android.os.AsyncTask;

import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordScoreList;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordStats;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordStatsList;
import com.antoshkaplus.words.backend.dictionaryApi.model.Version;
import com.antoshkaplus.words.model.Stats;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 8/6/16.
 */
public class StatsUpdateLocal implements Callable<SyncResult> {

    TranslationRepository repo;
    Context ctx;
    String account;


    StatsUpdateLocal(Context ctx, String account) {
        repo = new TranslationRepository(ctx);
        this.ctx = ctx;
        this.account = account;
    }


    public SyncResult call() {

        GoogleAccountCredential credential = CredentialFactory.create(ctx, account);

        DictionaryApi.Builder builder = new DictionaryApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                credential);

        builder.setRootUrl(BuildConfig.HOST);
        builder.setApplicationName("antoshkaplus-words");
        final DictionaryApi api = builder.build();

        PropertyStore store = new PropertyStore(ctx);

        SyncResult r = SyncResult.SUCCESS;
        try {
            Version backendVersion = api.getVersion().execute();
            int lastUpdateVersion = store.getStatsLastUpdateVersion();
            final ForeignWordStatsList stats = api.getStatsListGVersion(lastUpdateVersion).execute();
            // we have to create transaction in repo.
            Callable<Void> c = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (stats.getList() == null) {
                        return null;
                    }
                    for (ForeignWordStats fs : stats.getList()) {
                        Stats s = new Stats(fs.getForeignWord());
                        s = repo.createIfNotExists(s);
                        s.serverScore.failure = fs.getFailureScore();
                        s.serverScore.success = fs.getSuccessScore();
                        repo.update(s.serverScore);
                    }
                    return null;
                }
            };
            repo.executeBatch(c);
            store.setStatsLastUpdateVersion(backendVersion.getVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
            r = SyncResult.FAILURE_UNKNOWN;
        }
        return r;
    }
}
