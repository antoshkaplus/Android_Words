package com.antoshkaplus.words.backend;

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

    Key<BackendUser> getKey() {
        return Key.create(BackendUser.class, email);
    }

    public String getEmail() {
        return email;
    }

    public int getVersion() {
        return version;
    }

    public void increaseVersion() {
        ++version;
    }



}
