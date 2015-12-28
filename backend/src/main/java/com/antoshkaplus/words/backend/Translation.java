package com.antoshkaplus.words.backend;

import com.googlecode.objectify.annotation.Container;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by antoshkaplus on 8/5/15.
 */
@Entity
public class Translation {
    @Id
    private Long id;
    @Container
    private ForeignWord foreignWord;
    private String nativeWord;

    public Translation() {}

    public Translation(ForeignWord foreignWord, String nativeWord) {
        this.foreignWord = foreignWord;
        this.nativeWord = nativeWord;
    }


    public String getNativeWord() {
        return nativeWord;
    }

    public void setNativeWord(String nativeWord) {
        this.nativeWord = nativeWord;
    }

    public ForeignWord getForeignWord() {
        return foreignWord;
    }

    public void setForeignWord(ForeignWord foreignWord) {
        this.foreignWord = foreignWord;
    }

}
