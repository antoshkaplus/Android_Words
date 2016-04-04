package com.antoshkaplus.words.backend;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import com.googlecode.objectify.condition.IfNotNull;

import java.util.Date;

/**
 * Created by antoshkaplus on 8/5/15.
 *
 * Translation - child of dictionary of specific user
 * We can't specify any meaningful id as nativeWord can repeat itself
 *
 * it's tempting to create boolean field "deleted" but
 * we can keep track of deleted item by having deletedDate Null or NotNull
 *
 */
@Entity
public class Translation {
    @Id
    private String id;
    @Index
    private String foreignWord;
    private String nativeWord;
    @Index
    private Date creationDate;
    @Index(IfNotNull.class)
    private Date deletionDate;
    @Parent
    Key<BackendUser> owner;

    // here we keep when record was created/deleted from
    // server point of view.

    // you want to set this variable at once on all records before
    // making transaction

    // this variable is another way of having version on each record
    // version is needed to be able to synchronize from multiple devices
    @Index
    private Date updateDate;

    public Translation() {}

    public Translation(String foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        resetId();
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
        return id;
    }

    Key<Translation> getKey() {
        return Key.create(owner, Translation.class, id);
    }

    public void resetId() {
        this.id = foreignWord + "_" + nativeWord;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public boolean isDeleted() {
        return deletionDate != null;
    }
}
