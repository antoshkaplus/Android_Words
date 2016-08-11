package com.antoshkaplus.words;

import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.model.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by antoshkaplus on 8/11/16.
 */
public class GuessWordGameFactory {

    private static int DEFAULT_GUESS_COUNT = 4;

    private final Random rng = new Random(System.currentTimeMillis());

    List<Translation> translations;
    GuessWordGameType gameType = GuessWordGameType.NativeWord;
    private int guessCount = DEFAULT_GUESS_COUNT;


    public GuessWordGameFactory(List<Translation> translations) {
        this.translations = translations;
    }

    public void switchGameType() { this.gameType = this.gameType.getAnother(); }

    GuessWordGame createNew() {
        List<Translation> guesses = new ArrayList<>(guessCount);
        int correctPosition = rng.nextInt(guessCount);
        for (int i = 0; i < guessCount; i++) {
            Collections.swap(translations, i, rng.nextInt(translations.size()-i) + i);
            Translation t = translations.get(i);
            guesses.add(t);
        }
        GuessWordGame game = new GuessWordGame(guesses, correctPosition, gameType);
        return game;
    }


}
