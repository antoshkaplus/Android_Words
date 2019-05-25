package com.antoshkaplus.words.backend.containers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 1/10/16.
 */
public class TranslationList {

    private List<com.antoshkaplus.words.backend.model.Translation> list;
    private String nextCursor;

    public TranslationList() {
        this.list = new ArrayList<>();
    }

    public TranslationList(List<com.antoshkaplus.words.backend.model.Translation> list) {
        this.list = list;
    }

    public TranslationList(List<com.antoshkaplus.words.backend.model.Translation> list, String nextCursor) {
        this.list = list;
        this.nextCursor = nextCursor;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public List<com.antoshkaplus.words.backend.model.Translation> getList() {
        return list;
    }
    public void setList(List<com.antoshkaplus.words.backend.model.Translation> list) {
        this.list = list;
    }

    public void resetId() {
        for (com.antoshkaplus.words.backend.model.Translation t : list) {
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
        for (com.antoshkaplus.words.backend.model.Translation t : list) {
            ids.add(t.getId());
        }
        return ids;
    }

    // could do it in endpoint method, but feels like it's better to do here
    // throws exception if verification fails
    public void verify() throws IllegalArgumentException {
        for (com.antoshkaplus.words.backend.model.Translation t : list) {
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
