package com.antoshkaplus.words;

import com.antoshkaplus.words.model.Translation;
import com.antoshkaplus.words.model.UserTranslation;
import com.j256.ormlite.dao.Dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 7/28/15.
 */
public class TranslationRepository {

    private static final String TAG = "TranslationRepository";

    private DatabaseHelper helper;
    // may have multiple accounts per db
    // probably email address
    private String user;

    public void AddTranslation(Translation translation) throws Exception {
        helper.getDao(Translation.class).create(translation);
        helper.getDao(UserTranslation.class).create(new UserTranslation(user, translation));
    }

    public List<Translation> getAllTranslations() throws Exception {
        Dao<UserTranslation, Void> userTranslations = helper.getDao(UserTranslation.class);
        List<UserTranslation> userTrs = userTranslations.queryForEq(UserTranslation.FIELD_USER, user);
        List<Translation> trs = new ArrayList<Translation>(userTrs.size());
        for (UserTranslation ut : userTrs) {
            trs.add(ut.translation);
        }
        return trs;
    }




}
