package com.antoshkaplus.words;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.antoshkaplus.words.model.Score;
import com.antoshkaplus.words.model.Stats;
import com.antoshkaplus.words.model.StatsUpdate;
import com.antoshkaplus.words.model.Translation;
import com.antoshkaplus.words.model.TranslationKey;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private Context ctx; // to broadcast

    public TranslationRepository(Context ctx) {
        this.ctx = ctx;
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
        notifyDatabaseChanged(Translation.TABLE_NAME);
    }

    // returns false if translation already exists
    public boolean addTranslation(Translation translation) throws Exception {
        try {
            helper.getDao(Translation.class).create(translation);
            notifyDatabaseChanged(Translation.TABLE_NAME);
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
                dao.update(s);
                s.localScore.failure += score;
                helper.getDao(Score.class).update(s.localScore);
                return null;
            }
        };
        executeBatch(c);
        notifyDatabaseChanged(Score.TABLE_NAME, Stats.TABLE_NAME);
    }

    public void increaseFailureScore(final String word, final int score) throws Exception {
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Dao<Stats, String> dao = helper.getDao(Stats.class);
                Stats s = new Stats(word);
                s = dao.createIfNotExists(s);
                dao.update(s);
                s.localScore.failure += score;
                helper.getDao(Score.class).update(s.localScore);
                return null;
            }
        };
        executeBatch(c);
        notifyDatabaseChanged(Score.TABLE_NAME, Stats.TABLE_NAME);
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

    public void createOrRefresh(Stats s) throws Exception {
        helper.getDao(Stats.class).createIfNotExists(s);
        helper.getDao(Stats.class).refresh(s);
    }




    public void update(Stats s) throws Exception {
        helper.getDao(Stats.class).update(s);
    }

    public void update(List<Stats> stats) throws Exception {
        for (Stats s : stats) {
            update(s);
        }
    }


    public <T> T executeBatch(Callable<T> callable) throws Exception {
        return helper.getDao(Translation.class).callBatchTasks(callable);
    }

    public void refreshTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).refresh(translation);
        notifyDatabaseChanged(Translation.TABLE_NAME);
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
        notifyDatabaseChanged(Translation.TABLE_NAME);
    }

    public void updateTranslation(final Translation translation) throws Exception {
        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Dao<Translation, Long> dao = helper.getDao(Translation.class);
                    Translation t = dao.queryBuilder()
                            .selectColumns(Translation.FIELD_NAME_VERSION)
                            .where().idEq(translation.id).queryForFirst();
                    translation.version = t.version;
                    translation.increaseVersion();
                    dao.update(translation);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Dao<Translation, Long> dao = helper.getDao(Translation.class);
                    Translation t = dao.queryForId(translation.id);
                    throw new RuntimeException(ex);
                }
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
        boolean r = executeBatch(c);
        notifyDatabaseChanged(Translation.TABLE_NAME);
        return r;
    }

    void notifyDatabaseChanged(String... tableNames) {
        Intent i = new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
        DatabaseChangedReceiver.putTableNames(i, tableNames);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
    }

    List<StatsUpdate> getStatsUpdates() throws Exception {
        return helper.getDao(StatsUpdate.class).queryForAll();
    }

    void resetStatsUpdates() throws Exception {
        helper.getDao(StatsUpdate.class).deleteBuilder().delete();
    }

    public void prepareStatsUpdates() throws Exception {

        Callable<Object> c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Dao<Score, Long> scoreDao = helper.getDao(Score.class);
                Dao<Stats, String> statsDao = helper.getDao(Stats.class);
                Dao<StatsUpdate, String> updateDao = helper.getDao(StatsUpdate.class);

                QueryBuilder<Score, Long> scoreQB = scoreDao.queryBuilder();
                QueryBuilder<Stats, String> statsQB = statsDao.queryBuilder();

                List<Stats> stats = statsQB.join(scoreQB).where()
                        .ne(Score.FIELD_FAILURE, 0).or().ne(Score.FIELD_SUCCESS, 0).query();

                for (Stats s : stats) {
                    Score score = new Score();
                    s.localScore.moveTo(score);
                    // update in place to avoid looping second time
                    scoreDao.update(s.localScore);
                    StatsUpdate u = new StatsUpdate(s.foreignWord, score);
                    updateDao.create(u);
                }
                return null;
            }
        };
        executeBatch(c);

    }


}
