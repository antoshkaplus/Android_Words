package com.antoshkaplus.words.backend;


import com.antoshkaplus.words.backend.containers.WordVersusList;
import com.antoshkaplus.words.backend.model.BackendUser;
import com.antoshkaplus.words.backend.model.ForeignWordStats;
import com.antoshkaplus.words.backend.model.Translation;
import com.antoshkaplus.words.backend.model.Update;
import com.antoshkaplus.words.backend.model.Word;
import com.antoshkaplus.words.backend.model.WordVersus;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Result;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.googlecode.objectify.ObjectifyService.ofy;


@ApiClass(resource = "word")
public class WordEndpoint extends BaseEndpoint {

    static {
        ObjectifyService.register(WordVersus.class);
        ObjectifyService.register(Word.class);
    }

    @ApiMethod
    public WordVersus addWordVersus(WordVersus wordVersus, User user) {
        if (wordVersus.creationDate == null) {
            wordVersus.creationDate = new Date();
        }
        if (wordVersus.description == null)
        {
            throw new RuntimeException("description is empty");
        }
        if (wordVersus.words.stream().anyMatch(Objects::isNull))
        {
            throw new RuntimeException("no content in word");
        }
        wordVersus.owner = BackendUser.getKey(user);

        Result<Key<WordVersus>> wordVersusSave = ofy().save().entity(wordVersus);

        List<Word> words = wordVersus.words.stream()
                .map(w -> new Word(w, wordVersus.owner))
                .collect(Collectors.toList());

        // use transaction
        Map<Key<Word>, Word> wordMap = ofy().load().entities(words);
        words = words.stream()
                .map(w -> {
                    Word storeW = wordMap.get(w.getKey());
                    if (storeW != null) return storeW;
                    return w;
                })
                .collect(Collectors.toList());

        wordVersusSave.now();

        words.forEach(w -> w.wordVersusList.add(Ref.create(wordVersus)));
        ofy().save().entities(words).now();

        return wordVersus;
    }

    @ApiMethod
    public WordVersusList getWordVersusList(@Named("word") String wordStr, User user) {

        BackendUser backendUser = retrieveBackendUser(user);

        Word word = ofy().load().type(Word.class).parent(backendUser).id(wordStr).now();
        if (word == null) return new WordVersusList();

        List<WordVersus> wv = ofy().load().refs(word.wordVersusList).values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (wv.size() != word.wordVersusList.size())
        {
            word.wordVersusList = wv.stream()
                    .map(Ref::create)
                    .collect(Collectors.toList());
            ofy().save().entity(word).now();
        }

        return new WordVersusList(wv);
    }

    @ApiMethod
    public WordVersusList getWordVersusListWhole(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        return new WordVersusList(ofy().load().type(WordVersus.class).ancestor(backendUser).list());
    }
}
