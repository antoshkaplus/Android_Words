package com.antoshkaplus.words.backend.remote;


import com.antoshkaplus.words.backend.BackendUser;
import com.antoshkaplus.words.backend.Dictionary;
import com.antoshkaplus.words.backend.Translation;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.ObjectifyService;
//import com.googlecode.objectify.ObjectifyService;

import static com.googlecode.objectify.ObjectifyService.ofy;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
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
        RemoteApiOptions options = new RemoteApiOptions()
                .server("antoshkaplus-words.appspot.com", 443)
                .credentials(username, password);
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



    private void backup(Writer writer) throws IOException {
        JSONObject obj = collectJsonBackup();
        obj.writeJSONString(writer);
    }


}
