package com.antoshkaplus.words.remote;


import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.management.Query;


public class Words {


    public static void main(String[] args) {
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
        RemoteApiOptions options = new RemoteApiOptions()
                .server("antoshkaplus-recursivelists.appspot.com", 443)
                .credentials(username, password);
        RemoteApiInstaller installer = new RemoteApiInstaller();
        try {
            installer.install(options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            Entity entity = new Entity("Item");
            Query q = new Query("Item");
            PreparedQuery pq = ds.prepare(q);
            ArrayList<P> items = new ArrayList<>();
            for (Entity result : pq.asIterable()) {
                items.add(new P(
                        (String)result.getProperty("title"),
                        (String)result.getProperty("parentId")));
            }
            // looking for most
            items.sort((p1, p2) -> p1.parentId.compareTo(p2.parentId));
            F mostFrequent = new F();
            F current = new F();
            for (int i = 0; i < items.size(); ++i) {
                P p = items.get(i);
                if (!p.parentId.equals(current.id)) {
                    if (mostFrequent.count < current.count) {
                        mostFrequent = current;
                    }
                    current = new F(p.parentId, i, 0);
                }
                current.count += 1;
            }
            String id = mostFrequent.id;
            for (P p : items) {
                if (p.parentId.equals(id)) {
                    writer.println(p.title);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            installer.uninstall();
        }
    }

}
