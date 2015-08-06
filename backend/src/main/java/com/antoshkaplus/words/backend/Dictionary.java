package com.antoshkaplus.words.backend;

import java.util.List;

/**
 * Created by antoshkaplus on 8/5/15.
 */
public class Dictionary {

    List<ForeignWord> foreignWords;
    List<Translation> translations;


    public List<ForeignWord> getForeignWords() {
        return foreignWords;
    }

    public void setForeignWords(List<ForeignWord> foreignWords) {
        this.foreignWords = foreignWords;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }
}
