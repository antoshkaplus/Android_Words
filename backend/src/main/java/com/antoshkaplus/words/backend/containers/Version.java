package com.antoshkaplus.words.backend.containers;

/**
 * Created by antoshkaplus on 1/27/16.
 */
public class Version {
    private int version;

    public Version() {
        version = 0;
    }

    public Version(int version) {
        this.version = version;
    }

    void increase() {
        version += 1;
    }

    public int getVersion() {
        return version;
    }
}
