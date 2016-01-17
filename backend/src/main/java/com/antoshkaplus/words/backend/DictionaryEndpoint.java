/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.antoshkaplus.words.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

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

    static {
        ObjectifyService.register(ForeignWord.class);
        ObjectifyService.register(Translation.class);
        ObjectifyService.register(BackendUser.class);
    }

    public DictionaryEndpoint() {}

    @ApiMethod(name = "addForeignWordList", path = "add_foreign_word_list")
    public void addForeignWordList(ForeignWordList foreignWords, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        for (ForeignWord f : foreignWords.getList()) {
            f.setOwner(backendUser);
        }
        ofy().save().entities(foreignWords.getList()).now();
    }

    @ApiMethod(name = "getForeignWordList", path = "get_foreign_word_list")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public ForeignWordList getForeignWordList(User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        List<ForeignWord> words = ofy().load().type(ForeignWord.class).ancestor(backendUser).list();
        ForeignWordList list = new ForeignWordList(words);
        return list;
    }

    @ApiMethod(name = "addTranslation", path = "add_translation")
    public void addTranslation(Translation translation, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        translation.setOwner(backendUser);
        ofy().save().entity(translation).now();
    }



    @ApiMethod(name = "getTranslationList", path = "get_translation_list")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public TranslationList getTranslationList(User user) throws OAuthRequestException, InvalidParameterException {
        BackendUser backendUser = getBackendUser(user);
        List<Translation> translations = ofy().load().type(Translation.class).ancestor(backendUser).list();
        TranslationList list = new TranslationList(translations);
        return list;
    }


    @ApiMethod(name = "removeTranslation", path = "remove_translation")
    public void removeTranslation(Translation translation, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        translation.setOwner(backendUser);
        ofy().delete().entity(translation).now();
    }

    @ApiMethod(name = "addTranslationList", path = "add_translation_list")
    public void addTranslationList(TranslationList translationList, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        for (Translation t : translationList.getList()) {
            t.setOwner(backendUser);
        }
        ofy().save().entities(translationList.getList()).now();
    }

    private BackendUser getBackendUser(User user) {
        BackendUser backendUser = new BackendUser(user.getEmail());
        BackendUser b = ofy().load().entity(backendUser).now();
        if (b == null) {
            ofy().save().entity(backendUser).now();
        }
        return backendUser;
    }

    /*
    @ApiMethod(name = "uploadTranslationList", path = "upload_translation_list")
    public void uploadTranslationList() {
        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();
        service.getUploads()
    }
    */

}
