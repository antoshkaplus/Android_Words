package com.antoshkaplus.words;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.antoshkaplus.words.dialog.AddWordDialog;
import com.antoshkaplus.words.model.ForeignWord;

public class MainActivity extends Activity {

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


                addNewItem(string.toString(), pressedPosition);
                getListView().clearChoices();
            }
            @Override
            public void onAddStringDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }




}
