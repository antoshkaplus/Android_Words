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
import com.google.appengine.repackaged.com.google.common.base.Flag;
import com.google.appengine.repackaged.com.google.io.protocol.HtmlFormGenerator;
import com.google.appengine.repackaged.org.antlr.runtime.debug.TraceDebugEventListener;
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
import java.util.jar.Pack200;

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
        version = "v2",
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

    @ApiMethod(name = "getVersion", path = "get_version")
    public Version getVersion(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        return new Version(backendUser.getVersion());
    }

    @ApiMethod(name = "getTranslationListWhole", path = "get_translation_list_whole")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public TranslationList getTranslationListWhole(User user) throws OAuthRequestException, InvalidParameterException {
        BackendUser backendUser = retrieveBackendUser(user);
        List<Translation> translations = ofy().load().type(Translation.class).ancestor(backendUser).list();
        TranslationList list = new TranslationList(translations);
        return list;
    }

    @ApiMethod(name = "getTranslationList_GE_Timestamp", path = "get_translation_list_ge_timestamp")
    public TranslationList getTranslationList_GE_Timestamp(@Named("timestamp")Date timestamp, User user) {
        Query<Translation> query = getTranslationQuery(user);
        return new TranslationList(query.filter("updateDate >=", timestamp).list());
    }

    // returns all items with version high than given
    @ApiMethod(name = "getTranslationList_G_Version", path = "get_translation_list_g_version")
    public TranslationList getTranslationList_G_Version(@Named("version")Integer version, User user) {
        Query<Translation> query = getTranslationQuery(user);
        return new TranslationList(query.filter("version >", version).list());
    }

    @ApiMethod(name = "updateTranslationList", path = "update_translation_list")
    @SuppressWarnings("UnnecessaryLocalVariable")
    public ResourceBoolean updateTranslationList(@Named("dbVersion")final Integer version, final TranslationList translationList, final User user)
            throws OAuthRequestException, InvalidParameterException
    {
        // client doesn't know how to set id on new items and may forget to do add it on old ones
        translationList.resetId();
        translationList.verify();
        Integer v = ofy().transact(new Work<Integer>() {
            @Override
            public Integer run() {
                BackendUser backendUser = retrieveBackendUser(user);
                if (backendUser.getVersion() != version) {
                    return backendUser.getVersion();
                }

                for (Translation t : translationList.getList()) {
                    t.setOwner(backendUser);
                    t.setVersion(version);
                }
                ofy().save().entities(translationList.getList()).now();

                backendUser.increaseVersion();
                ofy().save().entity(backendUser).now();
                return version;
            }
        });
        return new ResourceBoolean(v.equals(version));
    }

    // use:
    //  update date and create date as now
    //  deleted = false
    //  have to check if already exists first

    // testing:
    //  put one translation inside
    //  put same translation inside : may need to check update/creation times later
    @ApiMethod(name = "addTranslationOnline", path = "add_translation_online")
    public void addTranslationOnline(final Translation shallowTranslation, final User user)
            throws OAuthRequestException, InvalidParameterException
    {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);

                final Translation tNew = new Translation(
                        shallowTranslation.getForeignWord(),
                        shallowTranslation.getNativeWord(),
                        backendUser);

                Translation tDb = ofy().load().type(Translation.class).parent(backendUser).id(tNew.getId()).now();
                tNew.setCreationDateToEarliest(tDb);
                ofy().save().entity(tNew);
            }
        });
    }


    private BackendUser retrieveBackendUser(User user) {
        BackendUser newUser = new BackendUser(user.getEmail());
        BackendUser res = ofy().load().entity(newUser).now();
        if (res == null) {
            ofy().save().entity(newUser).now();
            res = newUser;
        }
        return res;
    }

    private Query<Translation> getTranslationQuery(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        return ofy().load().type(Translation.class).ancestor(backendUser);
    }


}
