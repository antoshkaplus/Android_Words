package com.antoshkaplus.words.remote;


import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
//import com.googlecode.objectify.ObjectifyService;

//import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.management.Query;

public class Words {

    public static void main(String[] args) {
        /*
        Scanner scanner = new Scanner(System.in);
        String username = "";
        while (username.isEmpty()) {
            System.out.println("username: ");
            username = scanner.nextLine();

        }
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
        */
        RemoteApiOptions options = new RemoteApiOptions()
        //        .server("antoshkaplus-recursivelists.appspot.com", 443)
                .credentials("example@example.com", "haha")
                .server("localhost", 8080);
        RemoteApiInstaller installer = new RemoteApiInstaller();
        try {
            installer.install(options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

            //ofy().load().

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity("Translation", "haha");
        ds.put(entity);

        installer.uninstall();

    }

}
