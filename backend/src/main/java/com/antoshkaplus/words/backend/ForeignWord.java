package com.antoshkaplus.words.backend;

import java.util.Date;

/**
 * Created by antoshkaplus on 8/5/15.
 */
public class ForeignWord {

    private String word;
    private Date creationDate;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
