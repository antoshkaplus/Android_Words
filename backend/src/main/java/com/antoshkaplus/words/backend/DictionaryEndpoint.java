/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.antoshkaplus.words.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.io.protocol.HtmlFormGenerator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * An endpoint class we are exposing
 *
 * Endpoints open only auto generated setter and getter of a model
 * to Android client.
 *
 * Some properties have to be set in backend:
 * Translation id, owner
 */
@Api(name = "dictionaryApi",
        version = "v1",
        resource = "dictionary",
        namespace = @ApiNamespace(
                ownerDomain = "backend.words.antoshkaplus.com",
                ownerName = "backend.words.antoshkaplus.com"),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_HORSE, Constants.ANDROID_CLIENT_ID_PONY, Constants.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})
public class DictionaryEndpoint {

    static {
        ObjectifyService.register(Translation.class);
        ObjectifyService.register(BackendUser.class);
    }

    public DictionaryEndpoint() {}

    @ApiMethod(name = "addTranslation", path = "add_translation")
    public void addTranslation(Translation translation, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        translation.setOwner(backendUser);
        translation.resetId();
        ofy().save().entity(translation).now();
    }

    @ApiMethod(name = "getTranslationList", path = "get_translation_list")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public TranslationList getTranslationListWhole(User user) throws OAuthRequestException, InvalidParameterException {
        BackendUser backendUser = getBackendUser(user);
        List<Translation> translations = ofy().load().type(Translation.class).ancestor(backendUser).list();
        TranslationList list = new TranslationList(translations);
        return list;
    }

    public TranslationList getTranslationList(@Named("timestamp")Date timestamp, User user) {
        TranslationList list = new TranslationList();
        return list;
    }

    public void removeTranslationList(TranslationList list, User user) {


    }


    @ApiMethod(name = "removeTranslation", path = "remove_translation")
    public void removeTranslation(Translation translation, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        translation.setOwner(backendUser);
        translation.resetId();
        ofy().delete().entity(translation).now();
    }

    @ApiMethod(name = "addTranslationList", path = "add_translation_list")
    public void addTranslationList(TranslationList translationList, User user)
            throws OAuthRequestException, InvalidParameterException {

        BackendUser backendUser = getBackendUser(user);
        for (Translation t : translationList.getList()) {
            t.setOwner(backendUser);
            t.resetId();
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

    @ApiMethod(name = "getUpdateList", path = "get_update_list")
    public TranslationList getUpdateList(@Named("timestamp")Date timestamp, User user) {
        Query<Translation> query = ofy().load().type(Translation.class).ancestor(null);
        TranslationList list = new TranslationList();
        // can be duplicates
        list.getList().addAll(query.filter("deletionDate >=", timestamp).list());
        list.getList().addAll(query.filter("creationDate >=", timestamp).list());
        return list;
    }

    // we may throw exception here in case something went wrong.
    // that means unsuccessful operation
    @ApiMethod(name = "increaseDictionaryVersion", path = "increase_dictionary_version")
    // need more meaningful names here
    @SuppressWarnings("UnnecessaryLocalVariable")
    public Version increaseDictionaryVersion(final VersionTranslationList versionTranslationList, final @Named("timestamp")Date date, final User user)
            throws OAuthRequestException, InvalidParameterException
    {
        Version version = ofy().transact(new Work<Version>() {
            @Override
            public Version run() {
                BackendUser backendUser = ofy().load().key(Key.create(BackendUser.class, user.getEmail())).now();
                int currentVersion = backendUser.getVersion();
                if (currentVersion != versionTranslationList.version.getVersion()) {
                    return versionTranslationList.version;
                }
                clear(backendUser, date);
                try {
                    addTranslationList(versionTranslationList.list, user);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                backendUser.increaseVersion();
                ofy().save().entity(backendUser);
                return new Version(backendUser.getVersion());
            }
        });
        return version;
    }

    @ApiMethod(name = "getDictionaryVersion", path = "get_dictionary_version")
    public Version getDictionaryVersion(User user) {
        BackendUser backendUser = new BackendUser(user.getEmail());
        backendUser = ofy().load().entity(backendUser).now();
        return new Version(backendUser.getVersion());
    }

    // clear everything that has creation or deletion date older than passed one
    // for particular user
    private void clear(BackendUser user, Date date) {
        // maybe do this in batch
        Iterable<Key<Translation>> ts = ofy().load().type(Translation.class).ancestor(user).filter("deletionDate >=", date).keys().iterable();
        ofy().delete().keys(ts).now();
        ts = ofy().load().type(Translation.class).ancestor(user).filter("creationDate >=", date).keys().iterable();
        ofy().delete().keys(ts).now();
    }


    /*
    @ApiMethod(name = "uploadTranslationList", path = "upload_translation_list")
    public void uploadTranslationList() {
        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();
        service.getUploads()
    }
    */

}
