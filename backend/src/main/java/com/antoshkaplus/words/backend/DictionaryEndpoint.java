/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.antoshkaplus.words.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;

import java.security.InvalidParameterException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * An endpoint class we are exposing
 */
@Api(name = "dictionaryApi",
        version = "v1",
        resource = "dictionary",
        namespace = @ApiNamespace(
                ownerDomain = "backend.words.antoshkaplus.com",
                ownerName = "backend.words.antoshkaplus.com"),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class DictionaryEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "updateDictionary")
    public void updateDictionary(Dictionary dictionary, User user)
            throws OAuthRequestException, InvalidParameterException {

        String userId = user.getEmail();
        ofy().delete().key(Key.create(Dictionary.class, userId));
        Dictionary userDictionary = new Dictionary();
        userDictionary.setUserId(userId);
        userDictionary.setForeignWords(dictionary.getForeignWords());
        userDictionary.setTranslations(dictionary.getTranslations());
        // i probably have to save one by one
        ofy().save();
    }

}
