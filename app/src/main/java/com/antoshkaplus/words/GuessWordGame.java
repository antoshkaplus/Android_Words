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

    public enum Type {
        ForeignWord,
        NativeWord;

        private Type another;

        static {
            ForeignWord.another = NativeWord;
            NativeWord.another = ForeignWord;
        }

        public Type getAnother() {
            return another;
        }
    }

    private static final Random rng = new Random(System.currentTimeMillis());
    private List<Translation> translationList;

    private List<String> guesses;
    private String word;
    private int correctPosition;
    Type gameType = Type.NativeWord;
    private int guessCount;

    GuessWordGame(List<Translation> translationList, int guessCount) {
        this.translationList = translationList;
        this.guessCount = guessCount;
    }

    void NewGame() {
        guesses = new ArrayList<>(guessCount);
        correctPosition = rng.nextInt(guessCount);
        for (int i = 0; i < guessCount; i++) {
            Collections.swap(translationList, i, rng.nextInt(translationList.size()-i) + i);
            Translation t = translationList.get(i);
            if (correctPosition == i) {
                word = gameType == Type.NativeWord ? t.nativeWord : t.foreignWord;
            }
            guesses.add(gameType == Type.NativeWord ? t.foreignWord : t.nativeWord);
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

    public void setGameType(Type gameType) {
        this.gameType = gameType;
    }

    public Type getGameType() {
        return gameType;
    }

    public void switchGameType() { this.gameType = this.gameType.getAnother(); }
}
