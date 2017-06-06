/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.antoshkaplus.words.backend;

import com.antoshkaplus.words.backend.model.*;
import com.antoshkaplus.words.backend.model.BackendUser;
import com.antoshkaplus.words.backend.model.Translation;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
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
        ObjectifyService.register(ForeignWordStats.class);
        ObjectifyService.register(Translation.class);
        ObjectifyService.register(BackendUser.class);
        ObjectifyService.register(Update.class);
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

    @ApiMethod(name = "getTranslationList_Cursor", path = "get_translaiton_list_cursor")
    public TranslationList getTranslationList_Cursor(@Named("pageSize")Integer pageSize,
                                                     @Named("cursor") @Nullable String cursor, User user) {

        Query<Translation> q = getTranslationQuery(user).order("-updateDate");
        if (cursor != null && !cursor.isEmpty()) {
            q = q.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Translation> it = q.iterator();
        List<Translation> list = new ArrayList<>();
        while (it.hasNext() && list.size() < pageSize) {
            list.add(it.next());
        }
        String nextCursor = it.getCursor().toWebSafeString();
        return new TranslationList(list, nextCursor);
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




    @ApiMethod(name = "removeTranslationOnline", path = "remove_translation_online", httpMethod = "POST")
    public void removeTranslationOnline(final Translation shallowTranslation, final User user)
            throws OAuthRequestException, InvalidParameterException
    {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);

                final Translation tRem = new Translation(
                        shallowTranslation.getForeignWord(),
                        shallowTranslation.getNativeWord(),
                        backendUser);
                tRem.setDeleted(true);

                Translation tDb = ofy().load().type(Translation.class).parent(backendUser).id(tRem.getId()).now();
                tRem.setCreationDateToEarliest(tDb);
                ofy().save().entity(tRem).now();
            }
        });
    }

    // use:
    //  update date and create date as now
    //  deleted = false
    //  have to check if already exists first

    // testing:
    //  put one translation inside
    //  put same translation inside : may need to check update/creation times later
    // TODO increase db version
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
                ofy().save().entity(tNew).now();
            }
        });
    }

    @ApiMethod(name="getTranslationOnline", path="get_translation_online")
    public TranslationList getTranslationOnline(@Named("foreignWord") final String foreignWord, final User user)
        throws OAuthRequestException, InvalidParameterException {

        TranslationList list = ofy().transact(new Work<TranslationList>() {
            @Override
            public TranslationList run() {
                BackendUser u = retrieveBackendUser(user);
                return new TranslationList(new ArrayList<Translation>(ofy().load().type(Translation.class).ancestor(u).filter("foreignWord ==", foreignWord).list()));
            }
        });
        return list;
    }


    // with lambda those two methods would be awesome.
    // right now would have to use inheritance to code reuse.

    // why not use stats object over here. and load all changed stats all together

    @ApiMethod(name = "updateForeignWordStats", path = "update_foreign_word_stats")
    public void updateForeignWordStats(@Named("updateUUID")final String uuid, final ForeignWordStatsList list, final User user) {
        final BackendUser backendUser = retrieveBackendUser(user);
        final List<String> wordsList = list.getForeignWords();

        Update u = ofy().load().type(Update.class).parent(backendUser).id(uuid).now();
        if (u != null) {
            return;
        }

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                backendUser.increaseVersion();
                Map<String, ForeignWordStats> m = ofy().load().type(ForeignWordStats.class).parent(backendUser).ids(wordsList);
                for (int i = 0; i < wordsList.size(); ++i) {
                    ForeignWordStats s = list.getList().get(i);
                    String w = wordsList.get(i);
                    ForeignWordStats localS = m.get(w);
                    if (localS == null) {
                        localS = new ForeignWordStats(w);
                        localS.setOwner(backendUser);
                        m.put(w, localS);
                    }
                    localS.updateFrom(s);
                    localS.setVersion(backendUser.getVersion());
                }
                ofy().save().entities(m.values()).now();
                Update u = new Update(uuid, backendUser);
                ofy().save().entity(u).now();
                ofy().save().entity(backendUser).now();
            }
        });

    }

    // returns all items with version high than given
    // can figure out new version from elements
    @ApiMethod(name = "getStatsList_G_Version", path = "get_stats_list_g_version")
    public ForeignWordStatsList getStatsList_G_Version(@Named("version")Integer version, User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        Query<ForeignWordStats> query = ofy().load().type(ForeignWordStats.class).ancestor(backendUser);
        return new ForeignWordStatsList(query.filter("version >", version).list());
    }

    @ApiMethod(name = "getStatsListWhole", path = "get_stats_list_whole")
    public ForeignWordStatsList getStatsListWhole(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        List<ForeignWordStats> list = ofy().load().type(ForeignWordStats.class).ancestor(backendUser).list();
        return new ForeignWordStatsList(list);
    }



    @ApiMethod(name = "increaseSuccessScore", path = "increase_success_score")
    public void increaseSuccessScore(final ForeignWordScoreList list, final User user) {
        final BackendUser backendUser = retrieveBackendUser(user);
        final List<String> fwList = list.getForeignWords();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Map<String, ForeignWordStats> m = ofy().load().type(ForeignWordStats.class).parent(backendUser).ids(fwList);
                for (int i = 0; i < fwList.size(); ++i) {
                    ForeignWordScore f = list.getList().get(i);
                    String fw = fwList.get(i);
                    ForeignWordStats s = m.get(fw);
                    if (s == null) {
                        s = new ForeignWordStats(f.foreignWord);
                        s.setOwner(backendUser);
                        m.put(fw, s);
                    }
                    s.increaseSuccessScore(f.score);
                }
                ofy().save().entities(m.values()).now();
            }
        });
    }

    @ApiMethod(name = "increaseFailureScore", path = "increase_failure_score")
    public void increaseFailureScore(final ForeignWordScoreList list, final User user) {
        final BackendUser backendUser = retrieveBackendUser(user);
        final List<String> fwList = list.getForeignWords();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Map<String, ForeignWordStats> m = ofy().load().type(ForeignWordStats.class).parent(backendUser).ids(fwList);
                for (int i = 0; i < fwList.size(); ++i) {
                    ForeignWordScore f = list.getList().get(i);
                    String fw = fwList.get(i);
                    ForeignWordStats s = m.get(fw);
                    if (s == null) {
                        s = new ForeignWordStats(f.foreignWord);
                        s.setOwner(backendUser);
                        m.put(fw, s);
                    }
                    s.increaseFailureScore(f.score);
                }
                ofy().save().entities(m.values()).now();
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
