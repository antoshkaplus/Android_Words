package com.antoshkaplus.words.backend.containers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 7/25/16.
 */
public class ForeignWordScoreList {

    private List<ForeignWordScore> list = new ArrayList<>();

    public void setList(List<ForeignWordScore> list) {
        this.list = list;
    }

    public List<ForeignWordScore> getList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public List<String> getForeignWords() {
        List<String> words = new ArrayList<>(list.size());
        for (ForeignWordScore fws : list) {
            words.add(fws.foreignWord);
        }
        return words;
    }
}
