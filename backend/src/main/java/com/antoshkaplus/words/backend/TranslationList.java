package com.antoshkaplus.words.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 1/10/16.
 */
public class TranslationList {

    private List<Translation> list;

    public TranslationList() {
        this.list = new ArrayList<>();
    }

    public TranslationList(List<Translation> list) {
        this.list = list;
    }

    public List<Translation> getList() {
        return list;
    }
    public void setList(List<Translation> list) {
        this.list = list;
    }

    public void resetId() {
        for (Translation t : list) {
            if (t.getId() == null) {
                t.resetId();
            }
        }
    }

    public int size() {
        return list.size();
    }

    List<String> getIds() {
        List<String> ids = new ArrayList<>(list.size());
        for (Translation t : list) {
            ids.add(t.getId());
        }
        return ids;
    }

    // could do it in endpoint method, but feels like it's better to do here
    // throws exception if verification fails
    public void verify() throws IllegalArgumentException {
        for (Translation t : list) {
            // we don't check for id, as db would throw something in that case
            if (t.getUpdateDate() == null) {
                throw new IllegalArgumentException(t.getForeignWord() + " updateDate not present");
            }
            if (t.getCreationDate() == null) {
                throw new IllegalArgumentException(t.getForeignWord() + " creationDate not present");
            }
        }
    }



}
