package com.antoshkaplus.words;

import android.content.Context;

import com.antoshkaplus.words.model.Stats;
import com.antoshkaplus.words.model.Translation;
import com.antoshkaplus.words.model.TranslationKey;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.SelectArg;

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
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Void call() throws Exception {
                for (Translation t : translationList) {
                    addTranslation(t);
                }
                return null;
            }
        };
        executeBatch(c);
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

    // should be able to pass some kind of sorting parameters
    public AndroidDatabaseResults getStatsRawResults() throws Exception {
        return (AndroidDatabaseResults) helper.getDao(Stats.class).iterator().getRawResults();
    }

    public void increaseSuccessScore(final String word, final int score) throws Exception {
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Dao<Stats, String> dao = helper.getDao(Stats.class);
                Stats s = new Stats(word);
                s = dao.createIfNotExists(s);
                s.localScore.success += score;
                dao.update(s);
                return null;
            }
        };
        executeBatch(c);
    }

    public void increaseFailureScore(final String word, final int score) throws Exception {
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Dao<Stats, String> dao = helper.getDao(Stats.class);
                Stats s = new Stats(word);
                s = dao.createIfNotExists(s);
                s.localScore.failure += score;
                dao.update(s);
                return null;
            }
        };
        executeBatch(c);
    }

    // case insensitive by default
    public AndroidDatabaseResults getSuggestionTranslations(String scrap) throws Exception {
        String pattern = scrap + "%";
        SelectArg foreignPattern = new SelectArg(pattern);
        SelectArg nativePatten = new SelectArg(pattern);
        return (AndroidDatabaseResults) helper.getDao(Translation.class).queryBuilder().where().like(Translation.FIELD_FOREIGN_WORD, foreignPattern)
                .or().like(Translation.FIELD_NATIVE_WORD, nativePatten).iterator().getRawResults();
    }

    public Translation getTranslation(AndroidDatabaseResults results, int position) throws Exception {
        results.moveAbsolute(position);
        return helper.getDao(Translation.class).mapSelectStarRow(results);
    }

    public Stats getStats(AndroidDatabaseResults results, int position) throws Exception {
        results.moveAbsolute(position);
        return helper.getDao(Stats.class).mapSelectStarRow(results);
    }


    public <T> T executeBatch(Callable<T> callable) throws Exception {
        return helper.getDao(Translation.class).callBatchTasks(callable);
    }

    public void refreshTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).refresh(translation);
    }

    @SuppressWarnings("unchecked")
    public Translation getTranslation(TranslationKey key) throws Exception {
        TreeMap map = new TreeMap<String, String>();
        // to format quotes
        SelectArg foreignArg = new SelectArg(key.foreignWord);
        SelectArg nativeArg = new SelectArg(key.nativeWord);

        map.put(Translation.FIELD_FOREIGN_WORD, foreignArg);
        map.put(Translation.FIELD_NATIVE_WORD, nativeArg);
        List<Translation> list = helper.getDao(Translation.class).queryForFieldValues(map);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void deleteTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).delete(translation);
    }

    public void updateTranslation(final Translation translation) throws Exception {
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Dao<Translation, Long> dao = helper.getDao(Translation.class);
                Translation t = dao.queryBuilder()
                        .selectColumns(Translation.FIELD_NAME_VERSION)
                        .where().idEq(translation.id).queryForFirst();
                translation.version = t.version;
                translation.increaseVersion();
                dao.update(translation);
                return null;
            }
        };
        executeBatch(c);
    }

    public List<Translation> getTraslationList(Date date) throws Exception {
        return helper.getDao(Translation.class).queryBuilder().where().ge(Translation.FIELD_NAME_CREATION_DATE, date).query();
    }

    public List<Translation> getSyncedTranslationList(boolean synced) throws Exception {
        return helper.getDao(Translation.class).queryBuilder().where().eq(Translation.FIELD_NAME_SYNCED, synced).query();
    }

    public boolean trySyncTranslation(final Translation translation) throws Exception {
        // returns if success
        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Dao<Translation, Long> dao = helper.getDao(Translation.class);
                Translation t = dao.queryBuilder()
                        .selectColumns(Translation.FIELD_NAME_VERSION)
                        .where().idEq(translation.id).queryForFirst();
                if (t.version == translation.version) {
                    translation.synced = true;
                    translation.increaseVersion();
                    dao.update(translation);
                    return true;
                }
                return false;
            }
        };
        return executeBatch(c);
    }

}
