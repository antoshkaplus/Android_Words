package com.antoshkaplus.words;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;

import com.antoshkaplus.fly.dialog.OkDialog;
import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.ResourceBoolean;
import com.antoshkaplus.words.backend.dictionaryApi.model.Translation;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.backend.dictionaryApi.model.Version;
import com.antoshkaplus.words.model.TranslationKey;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 1/24/16.
 *
 * when we come here everything should be ready for processing.
 *
 */
public class SyncTask extends AsyncTask<Void, Void, Boolean> {

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

    @Override
    protected Boolean doInBackground(Void... params) {

        boolean success = true;
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
            int localVersion = store.lastSyncVersion();

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

                TranslationMerger merger = new TranslationMerger(repo);
                List<Translation> mergedList = merger.merge(remoteList.getList());

                remoteList.setList(mergedList);

                ResourceBoolean r = api.updateTranslationList(remoteVersion, remoteList).execute();
                localVersion = remoteVersion;
                store.setLastSyncVersion(localVersion);

                if (r.getValue()) {
                    break;
                }
            }
        } catch (Exception ex) {
            success = false;
            ex.printStackTrace();
        }
        return success;
    }

    // called from UI thread
    @Override
    protected void onPostExecute(Boolean result) {
        if (listener == null) return;
        listener.onSyncFinish(result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSyncFinish(boolean success);
    }

}
