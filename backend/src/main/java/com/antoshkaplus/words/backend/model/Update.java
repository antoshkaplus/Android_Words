package com.antoshkaplus.words.backend.model;


import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * Created by antoshkaplus on 8/6/16.
 */
@Entity
public class Update {
    Date timestamp;
    @Id
    String uuid;

    @Parent
    Key<BackendUser> owner;

    public Update() {}

    public Update(String uuid, BackendUser user) {
        this.uuid = uuid;
        owner = user.getKey();
        timestamp = new Date();

    }

}
