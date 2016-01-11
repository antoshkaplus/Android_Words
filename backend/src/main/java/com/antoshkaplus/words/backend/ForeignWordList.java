package com.antoshkaplus.words.backend;

import java.util.List;

/**
 * Created by antoshkaplus on 1/10/16.
 */
public class ForeignWordList {

    private List<ForeignWord> list;

    public ForeignWordList() {}

    public ForeignWordList(List<ForeignWord> list) {
        this.list = list;
    }

    public List<ForeignWord> getList() {
        return list;
    }
    public void setList(List<ForeignWord> list) {
        this.list = list;
    }


}
