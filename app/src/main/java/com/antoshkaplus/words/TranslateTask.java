package com.antoshkaplus.words;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 1/28/16.
 */
class TranslateTask extends AsyncTask<String, Void, String> {
    String foreignWord;
    Listener listener;

    @Override
    protected String doInBackground(String... strings) {
        foreignWord = strings[0];
//            GoogleCredential credential = new GoogleCredential().setAccessToken(
//                    "oauth2:251166830439-0l5bm28ucq6mnhj92ti3s7v960e3h2ci.apps.googleusercontent.com");

        Translate t = new Translate.Builder(
                AndroidHttp.newCompatibleTransport(),
                new JacksonFactory(),
                null)
                .setTranslateRequestInitializer(new TranslateRequestInitializer("AIzaSyCpNJPGA_zTpriCby8-z4XyAwEllC9wRlM"))
                .setApplicationName("antoshkaplus-words")
                .build();
        List<String> ls = new ArrayList<>();
        ls.add(foreignWord);
        try {
            TranslationsListResponse response = t.translations().list(ls, "ru").execute();
            String nativeWord = response.getTranslations().get(0).getTranslatedText();
            return nativeWord;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onTranslateFinish(foreignWord, s);
    }


    public interface Listener {
        void onTranslateFinish(String foreignWord, String nativeWord);
    }
}
