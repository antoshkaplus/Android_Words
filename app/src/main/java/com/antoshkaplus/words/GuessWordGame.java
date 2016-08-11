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

    private List<String> guesses;
    // should optimize it later. now is irrelevant.
    private List<Translation> translationGuesses;

    private String word;
    private int correctPosition;
    GuessWordGameType gameType = GuessWordGameType.NativeWord;

    GuessWordGame(List<Translation> guesses, int correctPosition, GuessWordGameType type) {
        // initialize guesses and correct word
        this.correctPosition = correctPosition;
        translationGuesses = guesses;
        gameType = type;
        this.guesses = new ArrayList<>(guesses.size());
        for (int i = 0; i < guesses.size(); ++i) {
            if (type == GuessWordGameType.NativeWord) {
                this.guesses.add(guesses.get(i).foreignWord);
            } else {
                this.guesses.add(guesses.get(i).nativeWord);
            }
        }
        Translation t = guesses.get(correctPosition);
        word = type == GuessWordGameType.NativeWord ? t.nativeWord : t.foreignWord;
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

    public void setGameType(GuessWordGameType gameType) {
        this.gameType = gameType;
    }

    public GuessWordGameType getGameType() {
        return gameType;
    }

    String getForeignWord(int position) {
        return translationGuesses.get(position).foreignWord;
    }

}
