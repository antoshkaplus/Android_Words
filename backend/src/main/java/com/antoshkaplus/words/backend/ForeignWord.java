package com.antoshkaplus.words.backend;

import java.util.Date;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by antoshkaplus on 8/5/15.
 */
@Entity
public class ForeignWord {

    @Id
    private Long id;
    private String word;
    private Date creationDate;

    public ForeignWord() {}
    public ForeignWord(String word, Date creationDate) {
        this.word = word;
        this.creationDate = creationDate;
    }

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
