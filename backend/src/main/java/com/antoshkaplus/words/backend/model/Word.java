package com.antoshkaplus.words.backend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Word {
    @Parent
    public Key<BackendUser> owner;
    @Id
    public String id;

    public List<Ref<WordVersus>> wordVersusList;


    public Word() {}
    public Word(String id, Key<BackendUser> owner) {
        this.id = id;
        this.owner = owner;
        this.wordVersusList = new ArrayList<>();
    }

    public Key<Word> getKey() {
        return Key.create(owner, Word.class, id);
    }
}
