package com.antoshkaplus.words;

import com.antoshkaplus.words.model.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by antoshkaplus on 7/31/15.
 */
public class GuessWordGame {


    private static final Random rng = new Random(System.currentTimeMillis());
    private List<Translation> translationList;

    private List<String> guesses;
    private String word;
    private int correctPosition;

    private int guessCount;

    GuessWordGame(List<Translation> translationList, int guessCount) {
        this.translationList = translationList;
        this.guessCount = guessCount;
    }

    void NewGame() {
        guesses = new ArrayList<>(guessCount);
        correctPosition = rng.nextInt(guessCount);
        for (int i = 0; i < guessCount; i++) {
            // be sure to use Vector.remove() or you may get the same item twice
            Collections.swap(translationList, i, rng.nextInt(translationList.size()-i) + i);
            Translation t = translationList.get(i);
            if (correctPosition == i) {
                word = t.nativeWord;
            }
            guesses.add(t.foreignWord);
        }
    }

    boolean IsCorrect(int position) {
        return correctPosition == position;
    }

    int getCorrectPosition() {
        return correctPosition;
    }

    List<String> getGuesses() {
        return guesses;
    }

    String getWord() {
        return word;
    }


}
