package com.antoshkaplus.words.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 7/30/16.
 */
public class ForeignWordStatsList {

    private List<ForeignWordStats> list = new ArrayList<>();


    ForeignWordStatsList() {}

    ForeignWordStatsList(List<ForeignWordStats> list) {
        this.list = list;
    }

    public void setList(List<ForeignWordStats> list) {
        this.list = list;
    }

    public List<ForeignWordStats> getList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    List<String> getForeignWords() {
        List<String> words = new ArrayList<>(list.size());
        for (ForeignWordStats fws : list) {
            words.add(fws.getForeignWord());
        }
        return words;
    }
}
