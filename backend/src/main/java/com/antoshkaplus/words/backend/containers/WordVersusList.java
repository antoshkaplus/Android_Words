package com.antoshkaplus.words.backend.containers;

import com.antoshkaplus.words.backend.model.WordVersus;

import java.util.ArrayList;
import java.util.List;

public class WordVersusList {

    public List<WordVersus> list;

    public WordVersusList() {
        this.list = new ArrayList<>();
    }
    public WordVersusList(List<WordVersus> list) {
        this.list = list;
    }
}
