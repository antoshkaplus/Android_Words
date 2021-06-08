package com.antoshkaplus.words.backend;

import com.antoshkaplus.words.backend.model.BackendUser;
import com.antoshkaplus.words.backend.model.Translation;
import com.antoshkaplus.words.backend.model.WordVersus;
import com.antoshkaplus.words.backend.model.ForeignWordStats;
import com.google.appengine.api.oauth.OAuthRequestException;

import java.security.InvalidParameterException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/17/16.
 *
 * We create this class to use some common functionality between remote API and endpoints
 */
public class Dictionary {

    @SuppressWarnings("UnnecessaryLocalVariable")
    public List<Translation> getTranslationList(BackendUser user)
            throws OAuthRequestException, InvalidParameterException {

        List<Translation> translations = ofy().load().type(Translation.class).ancestor(user).list();
        return translations;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public List<WordVersus> getWordVersusListWhole(BackendUser user) {
        List<WordVersus> list = ofy().load().type(WordVersus.class).ancestor(user).list();
        return list;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public List<ForeignWordStats> getForeignWordStats(BackendUser user) {
        List<ForeignWordStats> list = ofy().load().type(ForeignWordStats.class).ancestor(user).list();
        return list;
    }
}
