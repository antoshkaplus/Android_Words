package com.antoshkaplus.words.backend.model;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by antoshkaplus on 1/10/16.
 *
 * it's best to keep version in it's user's entity group
 * because it's changing too often and may cause contention
 * if application is used by many users
 */
@Entity
public class BackendUser {
    @Id
    private String email;
    private int version = 0;

    public BackendUser() {}

    public BackendUser(String email) {
        this.email = email;
    }

    public Key<BackendUser> getKey() {
        return Key.create(BackendUser.class, email);
    }

    public String getEmail() {
        return email;
    }

    public int getVersion() {
        return version;
    }

    public int increaseVersion() {
        return ++version;
    }


    public static Key<BackendUser> getKey(User user) {
        return Key.create(BackendUser.class, user.getEmail());
    }
}
