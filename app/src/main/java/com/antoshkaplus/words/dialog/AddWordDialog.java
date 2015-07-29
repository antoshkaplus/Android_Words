package com.antoshkaplus.words.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.antoshkaplus.words.R;

/**
 * Created by antoshkaplus on 7/29/15.
*/
public class AddWordDialog extends DialogFragment {

    private static final String TAG = "AddWordDialog";

    public static final String ARG_FOREIGN_WORD = "arg_foreign_word";
    public static final String ARG_NATIVE_WORD = "arg_native_word";

    private CharSequence title;
    private CharSequence hint;
    private EditText foreignWord;
    private EditText nativeWord;

    private AddWordDialogListener listener = new DefaultListener();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
//        title = args.getString(ARG_TITLE, "");
//        hint = args.getString(ARG_HINT, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_add_word_dialog, null);
        foreignWord = (EditText)view.findViewById(R.id.foreign_word);
        //input.setHint(hint);
        foreignWord.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        listener.onAddStringDialogSuccess(foreignWord.getText());
                        dismiss();
                        return true;
                    }
                    if (i == KeyEvent.KEYCODE_BACK) {
                        listener.onAddStringDialogCancel();
                        dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        foreignWord.setSingleLine();
        foreignWord.setImeActionLabel("Add", 0);
        foreignWord.requestFocus();
        Dialog dialog = (new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onAddStringDialogSuccess(foreignWord.getText());
                        dismiss();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onAddStringDialogCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setAddWordDialogListener(AddWordDialogListener listener) {
        this.listener = listener;
    }

    private class DefaultListener implements AddWordDialogListener {
        @Override
        public void onAddStringDialogSuccess(CharSequence string) {}
        @Override
        public void onAddStringDialogCancel() {}
    }

    public interface AddWordDialogListener {
        void onAddStringDialogSuccess(CharSequence string);
        void onAddStringDialogCancel();
    }
}
