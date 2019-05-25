package com.antoshkaplus.words.backend;

import com.antoshkaplus.words.backend.model.BackendUser;
import com.antoshkaplus.words.backend.model.Translation;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(name = "dictionaryApi",
        version = "v3",
        resource = "dictionary",
        namespace = @ApiNamespace(
                ownerDomain = "backend.words.antoshkaplus.com",
                ownerName = "backend.words.antoshkaplus.com"),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_HORSE, Constants.ANDROID_CLIENT_ID_PONY, Constants.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class BaseEndpoint {

    BackendUser retrieveBackendUser(User user) {
        BackendUser newUser = new BackendUser(user.getEmail());
        BackendUser res = ofy().load().entity(newUser).now();
        if (res == null) {
            ofy().save().entity(newUser).now();
            res = newUser;
        }
        return res;
    }

    Query<Translation> getTranslationQuery(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        return ofy().load().type(Translation.class).ancestor(backendUser);
    }


}
