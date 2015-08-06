/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.antoshkaplus.words.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.security.InvalidParameterException;
import java.util.List;


/**
 * An endpoint class we are exposing
 */
@Api(name = "dictionaryApi", version = "v1",
        scopes = {Constants.EMAIL_SCOPE}, clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class DictionaryEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "dictionary.update")
    public void updateDictionary(Dictionary dictionary, User user)
            throws OAuthRequestException, InvalidParameterException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.delete(KeyFactory.createKey("Dictionary", user.getUserId()));
        Entity userDictionary = new Entity("Dictionary", user.getUserId());
        datastore.put(userDictionary);
        for (Translation t : dictionary.getTranslations()) {
            Entity translation = new Entity("Translation", userDictionary.getKey());
            translation.setProperty("foreignWord", t.getForeignWord());
            translation.setProperty("nativeWord", t.getNativeWord());
            datastore.put(translation);
        }
        for (ForeignWord f : dictionary.getForeignWords()) {
            Entity foreignWord = new Entity("ForeignWord", userDictionary.getKey());
            foreignWord.setProperty("foreignWord", f.getWord());
            foreignWord.setProperty("creationDate", f.getCreationDate());
            datastore.put(foreignWord);
        }
    }

}
