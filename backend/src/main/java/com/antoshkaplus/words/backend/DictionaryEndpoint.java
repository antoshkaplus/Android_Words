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
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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


    @ApiMethod(name = "getTranslationListWhole", path = "get_translation_list_whole")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public TranslationList getTranslationListWhole(User user) throws OAuthRequestException, InvalidParameterException {
        BackendUser backendUser = getBackendUser(user);
        List<Translation> translations = ofy().load().type(Translation.class).ancestor(backendUser).list();
        TranslationList list = new TranslationList(translations);
        return list;
    }

    @ApiMethod(name = "getTranslationList", path = "get_translation_list")
    public TranslationList getTranslationList(@Named("timestamp")Date timestamp, User user) {
        Query<Translation> query = ofy().load().type(Translation.class).ancestor(null);
        return new TranslationList(query.filter("updateDate >=", timestamp).list());
    }

    @ApiMethod(name = "getTranslationListByVersion", path = "get_translation_list_by_version")
    public getTranslationList(@Named("version")Integer version, User user) {

    }


    // we may throw exception here in case something went wrong.
    // that means unsuccessful operation
    @ApiMethod(name = "updateTranslationList", path = "update_translation_list")
    // need more meaningful names here
    @SuppressWarnings("UnnecessaryLocalVariable")
    public void updateTranslationList(final TranslationList translationList, final User user)
            throws OAuthRequestException, InvalidParameterException
    {
        // client doesn't know how to set id on new items and may forget to do add it on old ones
        translationList.resetId();
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = getBackendUser(user);
                Map<String, Translation> m = ofy().load().type(Translation.class).parent(backendUser).ids(translationList.getIds());

                List<Translation> updates = new ArrayList<Translation>();
                for (Translation t : translationList.getList()) {
                    Translation r = m.get(t.getId());
                    if (r != null) {
                        Date rU = r.getUpdateDate();
                        Date tU = t.getUpdateDate();
                        if (rU.after(tU)) {
                            continue;
                        }
                    }
                    t.setOwner(backendUser);
                    t.setVersion(backendUser.getVersion());
                    updates.add(t);
                }
                ofy().save().entities(updates).now();
                backendUser.increaseVersion();
                ofy().save().entity(backendUser);
            }
        });
    }

    @ApiMethod(name = "updateTranslation", path = "update_translation")
    public void updateTranslation( Translation translation, final User user )
        throws OAuthRequestException, InvalidParameterException
    {
        // try to add / remove only one Translation.
        // good for users who work online and don't want to change too much stuff
    }


//    @ApiMethod(name = "getDictionaryVersion", path = "get_dictionary_version")
//    public Version getDictionaryVersion(User user) {
//        BackendUser backendUser = new BackendUser(user.getEmail());
//        backendUser = ofy().load().entity(backendUser).now();
//        return new Version(backendUser.getVersion());
//    }

    // clear everything that has creation or deletion date older than passed one
    // for particular user
//    private void clear(BackendUser user, Date date) {
//        // maybe do this in batch
//        Iterable<Key<Translation>> ts = ofy().load().type(Translation.class).ancestor(user).filter("deletionDate >=", date).keys().iterable();
//        ofy().delete().keys(ts).now();
//        ts = ofy().load().type(Translation.class).ancestor(user).filter("creationDate >=", date).keys().iterable();
//        ofy().delete().keys(ts).now();
//    }

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
