package com.antoshkaplus.words;

import android.content.BroadcastReceiver;
import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by antoshkaplus on 8/11/16.
 */
public abstract class BackPressedReceiver extends BroadcastReceiver {

    public static String ACTION_BACK_PRESSED = "com.antoshkaplus.words.BACK_PRESSED";

}
