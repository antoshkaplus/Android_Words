package com.antoshkaplus.words.backend;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

/**
 * Created by antoshkaplus on 8/5/15.
 *
 * Translation - child of dictionary of specific user
 * We can't specify any meaningful id as nativeWord can repeat itself
 * Index for nativeWord should be added automatically by GAE
 */
@Entity
public class Translation {
    @Id
    private String id;

    private String foreignWord;
    private String nativeWord;
    @Parent
    Key<BackendUser> owner;

    public Translation() {}

    public Translation(String foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        this.id = getId();
    }

    public Translation(String foreignWord, String nativeWord, Key<BackendUser> owner) {
        this(foreignWord, nativeWord);
        this.owner = owner;
    }

    public Translation(String foreignWord, String nativeWord, BackendUser owner) {
        this(foreignWord, nativeWord);
        this.owner = owner.getKey();
    }


    public String getNativeWord() {
        return nativeWord;
    }

    public void setNativeWord(String nativeWord) {
        this.nativeWord = nativeWord;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public void setForeignWord(String foreignWord) {
        this.foreignWord = foreignWord;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }

    public String getId() {
        return foreignWord + "_" + nativeWord;
    }

    Key<Translation> getKey() {
        return Key.create(owner, Translation.class, id);
    }
}
