package com.antoshkaplus.words.backend.remote;


import com.antoshkaplus.words.backend.BackendUser;
import com.antoshkaplus.words.backend.ForeignWord;
import com.antoshkaplus.words.backend.Translation;
import com.antoshkaplus.words.backend.TranslationList;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.ObjectifyService;
//import com.googlecode.objectify.ObjectifyService;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.management.Query;


// here we can actually test that everything works fine
public class Words {


    static {
        ObjectifyService.register(ForeignWord.class);
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
        addTranslation(new Translation("ff", "ff", user));
        int sz = getTranslationList().size();
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
        String filepath = "";
        while (filepath.isEmpty()) {
            System.out.println("output file:");
            filepath = scanner.nextLine();
        }
        RemoteApiOptions options = new RemoteApiOptions()
                .server("antoshkaplus-words.appspot.com", 443)
                .credentials(username, password);
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

}
