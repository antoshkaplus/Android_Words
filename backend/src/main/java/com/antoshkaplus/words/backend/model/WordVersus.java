package com.antoshkaplus.words.backend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;
import java.util.List;

@Entity
public class WordVersus {
    @Parent
    public Key<BackendUser> owner;
    @Id
    public Long id;

    public List<String> words;

    public String description;
    public Date creationDate;

    public Key<WordVersus> getKey() {
        return Key.create(owner, WordVersus.class, id);
    }
}
