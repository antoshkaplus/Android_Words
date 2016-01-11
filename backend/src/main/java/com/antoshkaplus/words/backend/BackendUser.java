package com.antoshkaplus.words.backend;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by antoshkaplus on 1/10/16.
 *
 *
 */
public class BackendUser {

    @Id
    private String email;

    public BackendUser() {}

    public BackendUser(String email) {
        this.email = email;
    }

    Key<BackendUser> getKey() {
        return Key.create(BackendUser.class, email);
    }
}
