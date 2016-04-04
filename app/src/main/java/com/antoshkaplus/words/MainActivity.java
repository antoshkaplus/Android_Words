package com.antoshkaplus.words;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.antoshkaplus.fly.dialog.OkDialog;
import com.antoshkaplus.fly.dialog.RetryDialog;
import com.antoshkaplus.words.backend.dictionaryApi.DictionaryApi;
import com.antoshkaplus.words.backend.dictionaryApi.model.ForeignWordList;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.dialog.AddWordDialog;
import com.antoshkaplus.words.model.Translation;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

public class MainActivity extends Activity implements
        GuessWordFragment.OnFragmentInteractionListener,
        TranslationListFragment.OnFragmentInteractionListener, SyncTask.Listener, TranslateTask.Listener {

    private static final String TAG = "MainActivity";
    private static final int GUESS_WORD_GAME_CHOICE_COUNT = 4;
    private static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 11;

    private TranslationRepository translationRepository;
    private Handler handler = new Handler();
    private Runnable nextGameEvent = null;

    private GuessWordGame game;

    private GuessWordFragment guessWordFragment;
    private TranslationListFragment translationListFragment;
    private AddWordDialog addWordDialog;

    private List<Translation> translationList;

    SharedPreferences settings;

    GoogleAccountCredential credential;

    SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref__account__key))) {
                ///onAccountChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credential = CredentialFactory.create(this, "antoshkaplus@gmail.com");
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(settingsListener);

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
            PropertyStore store = new PropertyStore(this);
            if (store.isFirstLaunch()) {
                PopulateWithInitialData();
                store.setFirstLaunch();
            }
//            ListView lv = (ListView)findViewById(R.id.translations);
            translationList = translationRepository.getAllTranslations();
//            lv.setAdapter(new TranslationAdapter(this, trs));

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.container, guessWordFragment);
            ft.commit();

            translationListFragment.setListAdapter(new TranslationAdapter(this, translationRepository));

            game = new GuessWordGame(translationList, GUESS_WORD_GAME_CHOICE_COUNT);
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
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
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
            } else if (id == R.id.action_sync) {
                if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                            PERMISSIONS_REQUEST_GET_ACCOUNTS);
                } else {
                    Sync();
                }
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


    void Sync() {
        new SyncTask(this).execute();
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
                Translation t = new Translation(from, to, new Date());
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
                    words[0], words[1], new Date()));
        }
        translationRepository.addTranslationList(ts);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void OnCorrectGuess(final GuessWordFragment fragment) {
        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext(fragment);
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    public void OnIncorrectGuess(final GuessWordFragment fragment) {
        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext(fragment);
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    @Override
    public void OnNext(GuessWordFragment fragment) {
        handler.removeCallbacks(nextGameEvent);
        game.NewGame();
        fragment.setGame(game);
    }


    private String retrieveAccount() {
        return settings.getString(getString(R.string.pref__account__key), null);
    }

    @Override
    public void onSyncFinish(boolean success) {
        // and this dialog logic that comes up
        FragmentManager mgr = getFragmentManager();
        int titleId = R.string.dialog__sync_success__title;
        int textId = R.string.dialog__sync_success__text;
        if (!success) {
            titleId = R.string.dialog__sync_failure__title;
            textId = R.string.dialog__sync_failure__text;
        }
        OkDialog.newInstance(
                getString(titleId), getString(textId)).show(mgr, "syncResult");
    }

    @Override
    public void onTranslateFinish(String foreignWord, String nativeWord) {
        addWordDialog.setTranslation(foreignWord, nativeWord);
    }

    /*
    private void onAccountChanged() {
        String account = retrieveAccount();
        credential.setSelectedAccountName(account);
        repository = new ItemRepository(this, account);
        try {
            parentId = rootId = repository.getRootId();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        loadItems();
        onItemsChanged();
    }
    */


}
