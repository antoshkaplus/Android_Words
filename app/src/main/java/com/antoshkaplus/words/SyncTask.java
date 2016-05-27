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
public class SyncTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private Listener listener;
    private TranslationRepository repo;

    public SyncTask(Context context) {
        this.context = context;
        repo = new TranslationRepository(context);
    }

    @Override
    protected Boolean doInBackground(String... params) {

        boolean success = true;
        // sometimes we have to try again this operation because of contention in the DB
        try {
            GoogleAccountCredential credential = CredentialFactory.create(context, params[0]);

            DictionaryApi.Builder builder = new DictionaryApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(),
                    credential);

            // later create something like configuration file
            //builder.setRootUrl("http://192.168.1.124:8080/_ah/api");
            builder.setApplicationName("antoshkaplus-words");
            final DictionaryApi api = builder.build();

            PropertyStore store = new PropertyStore(context);
            Date lastSuccessfulUpdate = store.lastSuccessfulUpdate();
            int localVersion = store.lastSyncVersion();



            for (;;) {
                Version v = api.getVersion().execute();
                int remoteVersion = v.getVersion();

                if (localVersion == remoteVersion) {
                    return true;
                }

                // we get changed translations
                TranslationList remoteUpdateList = api.getTranslationListGVersion(localVersion).execute();

                // we should lock database for awhile while I'm doing all necessary updates

                // merge to local
                // merges stuff with what we have on local
                // and returns new list that we would love to push to the server
                List<Translation> mergedList = merge(remoteUpdateList.getList());


                ResourceBoolean r = api.updateTranslationList(remoteVersion, mergedList);
                localVersion = remoteVersion;
                store.setLastSyncVersion(localVersion);

                if (r.getValue()) {
                    break;
                }

            }
        } catch (Exception ex) {
            success = false;
        }
        return success;
    }

    private boolean sync() {

    }


    private List<Translation> merge(final List<Translation> update, Date timestamp) throws Exception {
        TranslationMerger merger = new TranslationMerger(repo);
        merger.merge(update, timestamp);
    }




    // called from UI thread
    @Override
    protected void onPostExecute(Boolean result) {
        listener.onSyncFinish(result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }


    // this stuff should go away
    private com.antoshkaplus.words.model.Translation toModelTranslation(Translation tr) {
        com.antoshkaplus.words.model.Translation modelTr = new com.antoshkaplus.words.model.Translation(
                tr.getForeignWord(), tr.getNativeWord(), new Date(tr.getCreationDate().getValue()));
        return modelTr;
    }

    private Translation toRemoteTranslation(com.antoshkaplus.words.model.Translation localTr) {
        Translation remoteTr = new Translation();
        remoteTr.setCreationDate(new DateTime(localTr.creationDate.getTime()));
        remoteTr.setForeignWord(localTr.foreignWord);
        remoteTr.setNativeWord(localTr.nativeWord);
        return remoteTr;
    }

    public interface Listener {
        void onSyncFinish(boolean success);
    }

}
