package com.antoshkaplus.words;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;

import com.antoshkaplus.fly.dialog.OkDialog;
import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.Translation;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
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
            int lastSyncVersion = store.lastSyncVersion();

            TranslationList updateList = api.getTranslationList(new DateTime(lastSuccessfulUpdate)).execute();

            merge(updateList.getList());

            List<com.antoshkaplus.words.model.Translation> modelTrList = repo.getTraslationList(lastSuccessfulUpdate);
            updateList.getList().clear();
            for (com.antoshkaplus.words.model.Translation modelTr : modelTrList) {
                updateList.getList().add(toRemoteTranslation(modelTr));
            }

        } catch (Exception ex) {
            success = false;
        }
        return success;
    }

    private void merge(final List<Translation> update) throws Exception {
        Collections.sort(update, new Comparator<Translation>() {
            @Override
            public int compare(Translation lhs, Translation rhs) {
                long ld = lhs.getUpdateDate().getValue();
                long rd = rhs.getUpdateDate().getValue();
                // should not return long
                if (ld < rd) return -1;
                else if (ld == rd) return 0;
                else return 1;
            }
        });

        // merging updates from server and database in one transaction
        repo.executeBatch(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (Translation tr : update) {
                    TranslationKey key = new TranslationKey(tr.getForeignWord(), tr.getNativeWord());
                    com.antoshkaplus.words.model.Translation modelTr = toModelTranslation(tr);
                    // what if not there
                    boolean processed = false;
                    try {
                        repo.getTranslation(key);
                    } catch (Exception ex) {
                        // probably item is not there
                        if (!tr.getDeleted()) {
                            repo.addTranslation(modelTr);
                        }
                        processed = true;
                    }
                    if (!processed) {
                        long modelValue = modelTr.creationDate.getTime();
                        long value = value(tr);
                        if (tr.getDeleted()) {
                            if (modelValue < value) {
                                repo.deleteTranslation(modelTr);
                            }
                        } else {
                            if (modelValue > value) {
                                modelTr.creationDate.setTime(value);
                                repo.updateTranslation(modelTr);
                            }
                        }
                    }
                }
                return null;
            }
        });
    }


    // called from UI thread
    @Override
    protected void onPostExecute(Boolean result) {
        listener.onSyncFinish(result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

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
