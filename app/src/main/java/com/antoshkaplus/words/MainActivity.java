package com.antoshkaplus.words;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.antoshkaplus.words.dialog.AddWordDialog;
import com.antoshkaplus.words.dialog.RetryDialog;
import com.antoshkaplus.words.model.ForeignWord;
import com.antoshkaplus.words.model.NativeWord;
import com.antoshkaplus.words.model.Translation;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

public class MainActivity extends Activity implements
        GuessWordFragment.OnFragmentInteractionListener,
        TranslationListFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private TranslationRepository translationRepository;
    private Handler handler = new Handler();
    private GuessWordGame game;

    private GuessWordFragment guessWordFragment;
    private TranslationListFragment translationListFragment;
    private AddWordDialog addWordDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            guessWordFragment = (GuessWordFragment) getFragmentManager().getFragment(savedInstanceState, "guess_word_fragment");
            translationListFragment = (TranslationListFragment) getFragmentManager().getFragment(savedInstanceState, "translation_list_fragment");
        } else {
            guessWordFragment = new GuessWordFragment();
            translationListFragment = new TranslationListFragment();
        }
        setContentView(R.layout.activity_main);
        translationRepository = new TranslationRepository(this);
        try {
            translationRepository.Init("antoshkaplus@gmail.com");

            PopulateWithInitialData();

//            ListView lv = (ListView)findViewById(R.id.translations);
            List<Translation> trs = translationRepository.getAllTranslations();
//            lv.setAdapter(new TranslationAdapter(this, trs));

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.container, guessWordFragment);
            ft.commit();

            translationListFragment.setListAdapter(new TranslationAdapter(this, trs));

            game = new GuessWordGame(trs, 3);
            game.NewGame();
            guessWordFragment.setGame(game);
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        // i need to probably have some sort of callback



    }


    @Override
    protected void onResume() {
        super.onResume();
        if (translationListFragment == null || guessWordFragment == null) {
            translationListFragment = new TranslationListFragment();
            guessWordFragment = new GuessWordFragment();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            } else if (id == R.id.action_add_translation) {
                showAddWordDialog();
                return true;
            } else if (id == R.id.action_translation_list) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, translationListFragment);
                ft.commit();
                return true;
            } else if (id == R.id.action_guess_word) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, guessWordFragment);
                ft.commit();
                return true;
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        getFragmentManager().putFragment(outState, "guess_word_fragment", guessWordFragment);
        getFragmentManager().putFragment(outState, "translation_list_fragment", translationListFragment);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    public void showAddWordDialog() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        // now we have one dialog for everything
        addWordDialog = new AddWordDialog();
        Bundle args = new Bundle();
        args.putString(AddWordDialog.ARG_FOREIGN_WORD, getString(R.string.dialog__foreign_word__title));
        addWordDialog.setArguments(args);
        addWordDialog.setAddWordDialogListener(new AddWordDialog.AddWordDialogListener() {
            @Override
            public void onAddWordDialogSuccess(String from, String to) {
                RetryDialog.RetryDialogListener listener = new RetryDialog.RetryDialogListener() {
                    @Override
                    public void onDialogCancel() {
                    }

                    @Override
                    public void onDialogRetry() {
                        showAddWordDialog();
                    }
                };
                // empty string
                if (from.isEmpty() || to.isEmpty()) {
                    // show dialog with on retry
                    showRetryDialog(
                            getString(R.string.dialog__empty__title),
                            getString(R.string.dialog__empty__text),
                            listener);
                    return;
                }
                // this one could be made as separate function
                Translation t = new Translation(new ForeignWord(from, null), new NativeWord(to));
                // need to have my own exceptions
                try {
                    translationRepository.addTranslation(t);

                } catch (Exception ex) {
                    // should check for specific exception
                    showRetryDialog(
                            getString(R.string.dialog__exists__title),
                            getString(R.string.dialog__exists__text),
                            listener);

                }
            }

            @Override
            public void onAddWordDialogTranslate(String from) {
                new TranslateTask().execute(from);

                // here we have to use translate api
            }

            @Override
            public void onAddWordDialogCancel() {
            }
        });
        addWordDialog.show(ft, "dialog");
    }

    private void showRetryDialog(String title, String text, RetryDialog.RetryDialogListener listener) {
        RetryDialog dialog = (RetryDialog)getFragmentManager().findFragmentByTag("retry_dialog");
        if (dialog == null) {
            dialog = RetryDialog.newInstance(title, text);
        }
        dialog.setRetryDialogListener(listener);
        dialog.show(getFragmentManager(), "retry_dialog");
    }


    void PopulateWithInitialData() throws Exception {
        InputStream input = getResources().openRawResource(R.raw.words);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        // should check for empty string
        List<Translation> ts = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] words = line.split(";");
            ts.add(new Translation(
                    new ForeignWord(words[0], new Date()),
                    new NativeWord(words[1])
            ));
        }
        translationRepository.addTranslationList(ts);
    }


    public void OnCorrectGuess(final GuessWordFragment fragment) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                game.NewGame();
                fragment.setGame(game);
            }
        }, 2000);

    }

    public void OnIncorrectGuess(final GuessWordFragment fragment) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                game.NewGame();
                fragment.setGame(game);
            }
        }, 2000);
    }

    class TranslateTask extends AsyncTask<String, Void, String> {
        String foreignWord;

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
                    .setApplicationName("Words")
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
            addWordDialog.setTranslation(foreignWord, s);
        }
    }

}
