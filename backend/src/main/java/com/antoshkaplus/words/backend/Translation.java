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

    private Date creationDate;

    @Index
    private Date updateDate;
    private boolean deleted;

    @Index
    private int version;

    @Parent
    Key<BackendUser> owner;

    public Translation() {}

    public Translation(String foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        resetId();
        deleted = false;
        Date date = new Date();
        setCreationDate(date);
        setUpdateDate(date);
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

    public void resetCreationDate() {
        this.creationDate = new Date();
    }

    public void resetUpdateDate() {
        this.updateDate = new Date();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setCreationDateToEarliest(Translation t) {
        if (t == null) return;
        Date tD = t.creationDate;
        if (creationDate == null || (tD != null && creationDate.after(tD))) {
            creationDate = tD;
        }
    }
}
