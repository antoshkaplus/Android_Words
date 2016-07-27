package com.antoshkaplus.words.backend;

/**
 * Created by antoshkaplus on 7/23/16.
 */

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class ForeignWordStats {

    @Id
    private String foreignWord;

    private int successScore;
    private int failureScore;

    @Parent
    private Key<BackendUser> owner;


    ForeignWordStats(String foreignWord) {
        this.foreignWord = foreignWord;
        successScore = 0;
        failureScore = 0;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    void increaseSuccessScore(int delta) {
        successScore += delta;
    }

    void increaseFailureScore(int delta) {
        failureScore += delta;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }
}
