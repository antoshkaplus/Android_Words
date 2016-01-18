package com.antoshkaplus.words.backend;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by antoshkaplus on 1/10/16.
 *
 *
 */
@Entity
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

    public String getEmail() {
        return email;
    }

    //    public Key<BackendUser> getSuperKey() {
//        return null;
//    }
}
