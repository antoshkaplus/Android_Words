package com.antoshkaplus.words;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.antoshkaplus.words.dialog.AddWordDialog;
import com.antoshkaplus.words.model.ForeignWord;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TranslationRepository translationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button add =  (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pop dialog box that would let you add new translation for current user
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                //TranslateRequestInitializer i = new TranslateRequestInitializer("");
                GoogleCredential credential = new GoogleCredential().setAccessToken(
                        "oauth2:251166830439-0l5bm28ucq6mnhj92ti3s7v960e3h2ci.apps.googleusercontent.com");

                Translate t = new Translate.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new JacksonFactory(),
                        credential)
                        //.setTranslateRequestInitializer(new TranslateRequestInitializer("AIzaSyAIFPlEdDg6XEsRnZJels01EIdTmVBfRbM"))
                        .setApplicationName("Words")
                        .build();
                List<String> ls = new ArrayList<>();
                ls.add("add");
                try {
                    TranslationsListResponse response = t.translations().list(ls, "ru").execute();
                    for (TranslationsResource rs : response.getTranslations()) {
                        Log.d(TAG, rs.getTranslatedText());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showAddWordDialog() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        AddWordDialog dialog = new AddWordDialog();
        Bundle args = new Bundle();
        args.putString(AddWordDialog.ARG_FOREIGN_WORD, getString(R.string.dialog__foreign_word));
        dialog.setArguments(args);
        dialog.setAddWordDialogListener(new AddWordDialog.AddWordDialogListener() {
            @Override
            public void onAddStringDialogSuccess(CharSequence string) {
//                RetryDialog.RetryDialogListener listener = new RetryDialog.RetryDialogListener() {
//                    @Override
//                    public void onDialogCancel() { }
//                    @Override
//                    public void onDialogRetry() {
//                        showAddNewDialog();
//                    }
//                };
//                // empty string
//                if (string.toString().isEmpty()) {
//                    // show dialog with on retry
//                    showRetryDialog(
//                            getString(R.string.dialog__empty__title),
//                            getString(R.string.dialog__empty__text),
//                            listener);
//                    return;
//                }
//                // item already exists
//                boolean exists = false;
//                for (Item i : items) {
//                    if (i.title.contentEquals(string)) {
//                        exists = true;
//                        break;
//                    }
//                }
//                if (exists) {
//                    showRetryDialog(
//                            getString(R.string.dialog__exists__title),
//                            getString(R.string.dialog__exists__text),
//                            listener);
//                    return;
//                }


//                addNewItem(string.toString(), pressedPosition);
//                getListView().clearChoices();
            }
            @Override
            public void onAddStringDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }




}
