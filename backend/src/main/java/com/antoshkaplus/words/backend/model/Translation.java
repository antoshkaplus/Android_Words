package com.antoshkaplus.words.backend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    // if nativeWord is empty string, consider to be straightforward
    private String nativeWord;

    private Date creationDate;

    @Index
    private Date updateDate;
    private boolean deleted;

    @Index
    private int version;

    private List<Usage> usages = new ArrayList<>();

    @Parent
    Key<BackendUser> owner;

    private TranslationKind kind = TranslationKind.Word;


    public Translation() {}

    public Translation(String foreignWord, String nativeWord, TranslationKind kind) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
        this.kind = kind;
        resetId();
        deleted = false;
        Date date = new Date();
        setCreationDate(date);
        setUpdateDate(date);
    }

    public Translation(String foreignWord, String nativeWord, TranslationKind kind, Key<BackendUser> owner) {
        this(foreignWord, nativeWord, kind);
        this.owner = owner;

    }

    public Translation(String foreignWord, String nativeWord, TranslationKind kind, BackendUser owner) {
        this(foreignWord, nativeWord, kind);
        this.owner = owner.getKey();
    }


    public String getNativeWord() {
        if (nativeWord == null) return "";
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
        this.id = foreignWord + "_" + getNativeWord();
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

    public TranslationKind getKind() {
        return kind == null ? TranslationKind.Word : kind;
    }

    public void setKind(TranslationKind kind) {
        this.kind = kind;
    }

    public boolean emptyCreationDate() {
        return creationDate == null;
    }

    public boolean emptyUpdateDate() {
        return updateDate == null;
    }

    public List<Usage> getUsages() {
        return usages;
    }

    public void updateUsages(List<Usage> usages) {
        for (Usage u : usages) {
            if (this.usages.stream().anyMatch(x -> x.usage.equals(u.usage))) {
                continue;
            }
            this.usages.add(u);
        }
    }
}
