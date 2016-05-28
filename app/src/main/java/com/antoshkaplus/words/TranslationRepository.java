package com.antoshkaplus.words;

import android.content.Context;

import com.antoshkaplus.words.model.Translation;
import com.antoshkaplus.words.model.TranslationKey;
import com.j256.ormlite.android.AndroidDatabaseResults;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 7/28/15.
 *
 * we have to put logic that updates may be made only if updateDate is greater or equal to
 * lastSyncWith the server
 */
public class TranslationRepository {

    private static final String TAG = "TranslationRepository";

    private DatabaseHelper helper;


    public TranslationRepository(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public void addTranslationList(final List<Translation> translationList) throws Exception {
        for (Translation t : translationList) {
            addTranslation(t);
        }
    }

    // returns false if translation already exists
    public boolean addTranslation(Translation translation) throws Exception {
        try {
            helper.getDao(Translation.class).create(translation);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public List<Translation> getAllTranslations() throws Exception {
        return helper.getDao(Translation.class).queryForAll();
    }

    public AndroidDatabaseResults getTranslationsRawResults() throws Exception {
        return (AndroidDatabaseResults) helper.getDao(Translation.class).iterator().getRawResults();
    }

    public Translation getTranslation(AndroidDatabaseResults results, int position) throws Exception {
        results.moveAbsolute(position);
        return helper.getDao(Translation.class).mapSelectStarRow(results);
    }

    public void executeBatch(Callable<?> callable) throws Exception {
        helper.getDao(Translation.class).callBatchTasks(callable);
    }

    public void refreshTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).refresh(translation);
    }

    @SuppressWarnings("unchecked")
    public Translation getTranslation(TranslationKey key) throws Exception {
        TreeMap map = new TreeMap<String, String>();
        map.put(Translation.FIELD_FOREIGN_WORD, key.foreignWord);
        map.put(Translation.FIELD_NATIVE_WORD, key.nativeWord);
        List<Translation> list = helper.getDao(Translation.class).queryForFieldValues(map);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void deleteTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).delete(translation);
    }

    public void updateTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).update(translation);
    }

    public List<Translation> getTraslationList(Date date) throws Exception {
        return helper.getDao(Translation.class).queryBuilder().where().ge(Translation.FIELD_NAME_CREATION_DATE, date).query();
    }


}
