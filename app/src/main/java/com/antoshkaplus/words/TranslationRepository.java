package com.antoshkaplus.words;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.antoshkaplus.words.model.ForeignWord;
import com.antoshkaplus.words.model.NativeWord;
import com.antoshkaplus.words.model.Translation;
import com.antoshkaplus.words.model.User;
import com.antoshkaplus.words.model.UserTranslation;
import com.j256.ormlite.dao.Dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 7/28/15.
 */
public class TranslationRepository {

    private static final String TAG = "TranslationRepository";

    private DatabaseHelper helper;
    // may have multiple accounts per db
    // probably email address
    private User user;


    public TranslationRepository(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public void Init(String account) throws Exception {
        List<User> u = helper.getDao(User.class).queryForEq(User.FIELD_ACCOUNT, account);
        if (u.isEmpty()) {
            this.user = new User(account);
            helper.getDao(User.class).create(this.user);
        } else {
            this.user = u.get(0);
        }
    }

    public void addTranslationList(final List<Translation> translationList) throws Exception {
        helper.getDao(UserTranslation.class).callBatchTasks(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Translation t : translationList) {
                    addTranslation(t);
                }
                return null;
            }
        });
    }

    public boolean addTranslation(Translation translation) throws Exception {
        // seems I need to join actually
        if (translation.nativeWord.id == 0) {
            List<NativeWord> nw = helper.getDao(NativeWord.class).queryForEq(NativeWord.FIELD_NAME_WORD, translation.nativeWord.word);
            if (!nw.isEmpty()) {
                translation.nativeWord = nw.get(0);
            }
            // else would be inserted
        }
        // if id is already there we should be fine

        if (translation.foreignWord.id == 0) {
            List<ForeignWord> fw = helper.getDao(ForeignWord.class).queryForEq(ForeignWord.FIELD_NAME_WORD, translation.foreignWord.word);
            if (!fw.isEmpty()) {
                translation.foreignWord = fw.get(0);
            }
        }

        Map<String, Object> m = new HashMap<>();
        m.put(Translation.FIELD_FOREIGN_WORD, translation.foreignWord);
        m.put(Translation.FIELD_NATIVE_WORD, translation.nativeWord);

        // need id for current translation
        List<Translation> ts = helper.getDao(Translation.class).queryForFieldValues(m);
        if (ts.isEmpty()) {
            helper.getDao(Translation.class).create(translation);
        } else {
            translation = ts.get(0);
        }

        // now need to check if pair already exists here
        Map<String, Object> mu = new HashMap<>();
        mu.put(UserTranslation.FIELD_USER, user);
        mu.put(UserTranslation.FIELD_TRANSLATION, translation);

        if (helper.getDao(UserTranslation.class).queryForFieldValues(mu).isEmpty()) {
            helper.getDao(UserTranslation.class).create(new UserTranslation(user, translation));
            return true;
        } else {
            return false;
        }
    }

    public List<Translation> getAllTranslations() throws Exception {
        List<UserTranslation> uu = helper.getDao(UserTranslation.class).queryForAll();
        List<UserTranslation> userTrs = helper.getDao(UserTranslation.class).queryForEq(UserTranslation.FIELD_USER, user);
        List<Translation> trs = new ArrayList<Translation>(userTrs.size());
        for (UserTranslation ut : userTrs) {
            trs.add(ut.translation);
        }
        return trs;
    }




}
