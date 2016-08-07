package com.antoshkaplus.words;

import android.content.Context;
import android.os.AsyncTask;

import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.ResourceBoolean;
import com.antoshkaplus.words.backend.dictionaryApi.model.Translation;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.backend.dictionaryApi.model.Version;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 1/24/16.
 *
 * when we come here everything should be ready for processing.
 *
 */
public class SyncTask extends AsyncTask<Void, Void, SyncResult> {

    private Context context;
    private Listener listener;
    private TranslationRepository repo;
    // have to be initialized through constructor
    // to not forget to pass it to execute
    // also variable may be helpful later to pass it
    // to callbacks
    private String account;

    public SyncTask(Context context, String account) {
        this.context = context;
        this.account = account;
        repo = new TranslationRepository(context);
    }

    public enum Result {


    }

    // somehow on failure we have to provide more info about failure
    @Override
    protected SyncResult doInBackground(Void... params) {

        SyncResult result = SyncResult.SUCCESS;
        // sometimes we have to try again this operation because of contention in the DB
        try {
            GoogleAccountCredential credential = CredentialFactory.create(context, account);

            DictionaryApi.Builder builder = new DictionaryApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(),
                    credential);

            builder.setRootUrl(BuildConfig.HOST);
            builder.setApplicationName("antoshkaplus-words");
            final DictionaryApi api = builder.build();

            PropertyStore store = new PropertyStore(context);
            int localVersion = store.getLastSyncVersion();

            for (;;) {
                Version v = api.getVersion().execute();
                int remoteVersion = v.getVersion();


                TranslationList remoteList = new TranslationList();

                // if localVersion equals remoteVersion we still
                // have to update with unsync from local db
                if (localVersion != remoteVersion) {
                    remoteList = api.getTranslationListGVersion(localVersion).execute();
                }

                if (remoteList.getList() == null) {
                    remoteList.setList(new ArrayList<Translation>());
                }

                if (!isValid(remoteList)) {
                    result = SyncResult.FAILURE_SERVER_DATA_VALIDITY;
                    break;
                }

                TranslationMerger merger = new TranslationMerger(repo);
                List<Translation> mergedList = merger.mergeRemote(remoteList.getList());

                remoteList.setList(mergedList);

                ResourceBoolean r = api.updateTranslationList(remoteVersion, remoteList).execute();
                store.setLastSyncVersion(remoteVersion);
                localVersion = remoteVersion;


                if (r.getValue()) {
                    // it's tempting to increase remoteVersion because remote update
                    // was successful, but there is no guarantee that remote version
                    // actually changed
                    merger.onRemoteUpdateSuccess();
                    break;
                }

                // else there were changes in the db and we have to synchronize everything again
                // we want to avoid doing it on server side to keep logic in one place

                // we don't want to stress server with synchronization procedure too much
                // there are may be per user preferences
            }

            // top one should be runnable too.
            if (result != SyncResult.SUCCESS) {
                return result;
            }
            StatsUpdateBackend statsUpdateBackend = new StatsUpdateBackend(context, account);
            result = statsUpdateBackend.call();
            if (result != SyncResult.SUCCESS) {
                return result;
            }
            StatsUpdateLocal statsUpdateLocal = new StatsUpdateLocal(context, account);
            result = statsUpdateLocal.call();
            if (result != SyncResult.SUCCESS) {
                return result;
            }


        } catch (Exception ex) {
            result = SyncResult.FAILURE_UNKNOWN;
            ex.printStackTrace();
        }
        return result;
    }


    private boolean isValid(TranslationList translations) {
        for (Translation t : translations.getList()) {
            if (t.getCreationDate() == null || t.getUpdateDate() == null ||
                    t.getVersion() == null || t.getDeleted() == null ||
                    t.getForeignWord() == null || t.getNativeWord() == null) {
                return false;
            }
        }
        return true;
    }

    // called from UI thread
    @Override
    protected void onPostExecute(SyncResult result) {
        if (listener == null) return;
        listener.onSyncFinish(result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSyncFinish(SyncResult result);
    }

}
