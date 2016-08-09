package com.antoshkaplus.words.backend.model;

/**
 * Created by antoshkaplus on 7/23/16.
 */

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class ForeignWordStats {

    @Id
    private String foreignWord;

    private int successScore;
    private int failureScore;

    // have to keep version around for
    // synchronization purposes
    @Index
    private int version;

    @Parent
    private Key<BackendUser> owner;


    public ForeignWordStats() {}

    public ForeignWordStats(String foreignWord) {
        this.foreignWord = foreignWord;
        successScore = 0;
        failureScore = 0;
    }

    public void updateFrom(ForeignWordStats s) {
        successScore += s.successScore;
        failureScore += s.failureScore;
    }

    public void setFailureScore(int failureScore) {
        this.failureScore = failureScore;
    }

    public int getFailureScore() {
        return failureScore;
    }

    public void setSuccessScore(int successScore) {
        this.successScore = successScore;
    }

    public int getSuccessScore() {
        return successScore;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public void increaseSuccessScore(int delta) {
        successScore += delta;
    }

    public void increaseFailureScore(int delta) {
        failureScore += delta;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
