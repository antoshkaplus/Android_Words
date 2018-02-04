package com.antoshkaplus.words.backend.model;

/**
 * Created by antoshkaplus on 7/23/16.
 */

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

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

    @Index
    private int lookupCount;
    private Date lastLookup;

    public ForeignWordStats() {}

    public ForeignWordStats(String foreignWord) {
        this.foreignWord = foreignWord;
        successScore = 0;
        failureScore = 0;
        lookupCount = 0;
        lastLookup = new Date();
    }

    public void updateFrom(ForeignWordStats s) {
        successScore += s.successScore;
        failureScore += s.failureScore;
        lookupCount += s.lookupCount;
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

    public int getLookupCount() {
        return lookupCount;
    }

    public void setLookupCount(int lookupCount) {
        this.lookupCount = lookupCount;
    }

    public void increaseLookupCount() {
        ++this.lookupCount;
        this.lastLookup = new Date();
    }

    public Date getLastLookup() {
        return lastLookup;
    }
}
