package com.antoshkaplus.words.backend;

import java.util.List;

/**
 * Created by antoshkaplus on 1/10/16.
 */
public class TranslationList {

    private List<Translation> list;

    public TranslationList() {}

    public TranslationList(List<Translation> list) {
        this.list = list;
    }

    public List<Translation> getList() {
        return list;
    }
    public void setList(List<Translation> list) {
        this.list = list;
    }

}