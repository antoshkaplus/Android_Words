package com.antoshkaplus.words.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 8/5/15.
 */
@Entity
public class Dictionary {

    @Id
    private String userId;
    private List<ForeignWord> foreignWords = new ArrayList<>();
    private List<Translation> translations = new ArrayList<>();


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

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
