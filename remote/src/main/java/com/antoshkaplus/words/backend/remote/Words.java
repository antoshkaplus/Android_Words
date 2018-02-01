package com.antoshkaplus.words.backend.remote;


import com.antoshkaplus.words.backend.model.BackendUser;
import com.antoshkaplus.words.backend.Dictionary;
import com.antoshkaplus.words.backend.model.Translation;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import com.google.appengine.api.datastore.DatastoreService;


import static com.googlecode.objectify.ObjectifyService.ofy;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


// later start using endpoints
// here we can actually test that everything works fine
public class Words {

    static {
        ObjectifyService.register(Translation.class);
        ObjectifyService.register(BackendUser.class);
    }

    BackendUser user;
    RemoteApiInstaller installer;


    public static void main(String[] args) {
        Words words = new Words();
        words.run();
    }

    private void run() {
        init();
        // we don't really need to ask file name yet
        for (;;) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("type command:");
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            }
            if (command.equals("backup")) {
                System.out.println("file path:");
                String filePath = scanner.nextLine();
                try {
                    FileWriter writer = new FileWriter(filePath);
                    backup(writer);
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (command.equals("foreign")) {
                System.out.println("file path:");
                String filePath = scanner.nextLine();
                try {
                    FileWriter writer = new FileWriter(filePath);
                    //outputForeignWords(writer);
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (command.equals("new")) {
                newForeignWords();
            }
            if (command.equals("update")) {
                updateCreationDate();
            }
            if (command.equals("up2")) {
                updateCreationDate2();
            }
            if (command.equals("users")) {
                printUsers();
            }
            if (command.equals("fillmiss")) {
                fillMissingValues();
            }
            if (command.equals("add")) {
//                Translation t = new Translation("foreign", "native");
//                ofy().save().entity(t).now();
            }
            if (command.equals("size")) {
                int size = ofy().load().type(Translation.class).count();
                System.out.println(size);
            }
            if (command.equals("test")) {
//                Translation t = new Translation("foreign", "native");
//                ofy().save().entity(t).now();
//
//                ArrayList<String> ids = new ArrayList<>();
//                ids.add("foreign_native");
//                Map<String, Translation> ts = ofy().load().type(Translation.class).ids(ids);
//                System.out.println("test end");
            }
        }
        installer.uninstall();
    }

    private void init() {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        while (username.isEmpty()) {
            System.out.println("username: ");
            username = scanner.nextLine() + "@gmail.com";
        }
        user = new BackendUser(username);

        String password = "";
        while (password.isEmpty()) {
            System.out.println("password: ");
            password = scanner.nextLine();
        }

//        RemoteApiOptions options = new RemoteApiOptions()
//                .server("localhost", 8080).useDevelopmentServerCredential();

        RemoteApiOptions options = new RemoteApiOptions()
                .server("antoshkaplus-words.appspot.com", 443)
                .useApplicationDefaultCredential();
        // still may try to go to localhost
        //        .credentials("example@example.com", "haha")
        //        .server("localhost", 8080);

        installer = new RemoteApiInstaller();
        try {
            installer.install(options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ObjectifyService.begin();
    }

    private void addTranslation(Translation t) {
        ofy().save().entity(t).now();
    }

    private List<Translation> getTranslationList() {
        return ofy().load().type(Translation.class).ancestor(user).list();
    }

    // should be used only by administrator
    private List<BackendUser> getBackendUserList() {
        return ofy().load().type(BackendUser.class).list();
    }

    public JSONObject translationToJsonObject(Translation t) {
        JSONObject obj = new JSONObject();
        obj.put("foreignWord", t.getForeignWord());
        obj.put("nativeWord", t.getNativeWord());
        return obj;
    }

    JSONObject collectJsonBackup() {
        JSONObject root = new JSONObject();
        JSONArray users = new JSONArray();
        root.put("users", users);
        for (BackendUser bu : getBackendUserList()) {
            Dictionary dict = new Dictionary();
            JSONObject userObj = new JSONObject();
            userObj.put("email", bu.getEmail());
            try {
                JSONArray translations = new JSONArray();
                for (Translation t : dict.getTranslationList(bu)) {
                    translations.add(translationToJsonObject(t));
                }
                userObj.put("translations", translations);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            users.add(userObj);
        }
        return root;
    }

    private Map<String, Date> getForeignWords() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("ForeignWord");
        List<Entity> fs = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
        Map<String, Date> m = new HashMap<>(fs.size());
        for (Entity e : fs) {
            m.put(e.getKey().getName(), (Date)e.getProperty("creationDate"));
        }
        return m;
    }

    private Map<String, Translation> getTranslations() {
        List<Translation> ts = ofy().load().type(Translation.class).list();
        Map<String, Translation> m = new HashMap<>(ts.size());
        for (Translation t : ts) {
            m.put(t.getForeignWord(), t);
        }
        return m;
    }

    private void newForeignWords() {
        Map<String, Date> fws = getForeignWords();
        Map<String, Translation> ts = getTranslations();
        boolean noNew = true;
        for (Map.Entry<String, Date> e : fws.entrySet()) {
            if (!ts.containsKey(e.getKey())) {
                noNew = false;
                System.out.println(e.getKey() + e.getValue().getTime());
            }
        }
        if (noNew) {
            System.out.println("no new words");
        }
    }

    private void updateCreationDate() {
        Map<String, Translation> ts = getTranslations();
        Map<String, Date> fws = getForeignWords();

        System.out.println("translation count: " + ts.size());
        System.out.println("foreign words count: " + fws.size());

        List<Translation> updateList = new ArrayList<>(fws.size());
        for (Map.Entry<String, Date> e : fws.entrySet()) {
            Translation t = ts.get(e.getKey());
            t.setCreationDate(e.getValue());
            updateList.add(t);
        }
        ofy().save().entities(updateList).now();
    }

    private void printUsers() {
        BackendUser user = ofy().load().type(BackendUser.class).list().get(0);
        System.out.println(user.getEmail() + " " + user.getVersion());
    }


    private void backup(Writer writer) throws IOException {
        JSONObject obj = collectJsonBackup();
        obj.writeJSONString(writer);
    }

    // property deleted updated automatically
    private void updateCreationDate2() {
        List<Translation> ts = ofy().load().type(Translation.class).list();
        List<Translation> updateList = new ArrayList<>();
        for (Translation t : ts) {
            if (t.getCreationDate() == null) {
                t.setCreationDate(new Date());
                updateList.add(t);
            }
        }
        ofy().save().entities(updateList).now();
    }

    private void fillMissingValues() {
        com.googlecode.objectify.cmd.Query<Translation> q = ofy().load().type(Translation.class);
        List<Key<Translation>> cts = q.filter("creationDate ==", null).keys().list();
        List<Key<Translation>> uts = q.filter("updateDate ==", null).keys().list();

        HashSet<Key<Translation>> sts = new HashSet<>(cts);
        for (Key<Translation> t : uts) {
            sts.add(t);
        }

        Map<Key<Translation>, Translation> ts = ofy().load().keys(sts);

        for (Translation t : ts.values()) {
            if (t.emptyCreationDate()) {
                t.setCreationDate(new Date());
            }
            if (t.emptyUpdateDate()) {
                t.setUpdateDate(t.getCreationDate());
            }
        }
        ofy().save().entities(ts.values()).now();
    }
}
