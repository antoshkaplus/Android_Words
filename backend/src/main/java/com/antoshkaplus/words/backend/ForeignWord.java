package com.antoshkaplus.words.backend;

import java.util.Date;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

/**
 * Created by antoshkaplus on 8/5/15.
 *
 * id should be word itself as Dictionary of the user
 * is going to be parent object
 */
@Entity
public class ForeignWord {

    @Id
    private String word;
    private Date creationDate;
    @Parent
    Key<BackendUser> owner;

    public ForeignWord() {}
    public ForeignWord(String word, Date creationDate) {
        this.word = word;
        this.creationDate = creationDate;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }
}
