package com.antoshkaplus.words.backend.containers;

/**
 * Created by antoshkaplus on 8/6/16.
 */
public class VersionStatsList {

    Version version;
    ForeignWordStatsList list;

    void setVersion(Version version) {
        this.version = version;
    }

    void setList(ForeignWordStatsList list) {
        this.list = list;
    }

}
