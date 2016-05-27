package com.antoshkaplus.words;

import com.antoshkaplus.words.backend.dictionaryApi.model.Translation;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.model.TranslationKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 5/26/16.
 */

public class TranslationMerger {

    private Date localTimestamp;
    private List<Translation> remoteList;
    private List<Translation> mergedRemoteList;
    private TranslationRepository repo;



    static class LocalTranslationList {

        List<com.antoshkaplus.words.model.Translation> translationList;
        LocalTranslationComparator comparator;

        static class LocalTranslationComparator implements Comparator<com.antoshkaplus.words.model.Translation> {
            @Override
            public int compare(com.antoshkaplus.words.model.Translation t_0,
                               com.antoshkaplus.words.model.Translation t_1) {

                int comp = t_0.foreignWord.compareTo(t_1.foreignWord);
                if (comp == 0) {
                    comp = t_0.nativeWord.compareTo(t_0.nativeWord);
                }
                return comp;
            }
        };

        LocalTranslationList(List<com.antoshkaplus.words.model.Translation> translationList) {
            init(translationList);
        }

        void init(List<com.antoshkaplus.words.model.Translation> translationList) {
            this.translationList = translationList;
            comparator = new LocalTranslationComparator();
        }

        com.antoshkaplus.words.model.Translation extract(Translation remoteTranslation) {
            int index = findIndex(remoteTranslation);
            if (index < 0) return null;
            com.antoshkaplus.words.model.Translation t = translationList.get(index);
            translationList.remove(index);
            return  t;
        }

        // can return null if not found
        com.antoshkaplus.words.model.Translation find(Translation remoteTranslation) {
            int index = findIndex(remoteTranslation);
            if (index < 0) return null;
            return translationList.get(index);
        }

        int findIndex(Translation remoteTranslation) {
            com.antoshkaplus.words.model.Translation t = new com.antoshkaplus.words.model.Translation(
                    remoteTranslation.getForeignWord(),
                    remoteTranslation.getNativeWord()
            );
            return Collections.binarySearch(translationList, t, comparator);
        }


        int size() {
            return translationList.size();
        }

        List<com.antoshkaplus.words.model.Translation> getList() {
            return translationList;
        }
    }


    TranslationMerger(TranslationRepository repo) {
        this.repo = repo;
    }


    public List<Translation> merge(final List<Translation> remoteList, Date localTimestamp) {
        this.remoteList = remoteList;
        mergedRemoteList = new ArrayList<>(remoteList.size());
        this.localTimestamp = localTimestamp;

        // merging updates from server and database in one transaction
        repo.executeBatch(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
            mergeTransaction();
            }
        });
        return mergedRemoteList;
    }

    private void mergeTransaction() throws Exception {
        LocalTranslationList localList = new LocalTranslationList(repo.getTraslationList(localTimestamp));
        List<com.antoshkaplus.words.model.Translation> mergedLocalList = new ArrayList<>(localList.size());

        for (Translation r : remoteList) {
            com.antoshkaplus.words.model.Translation t = localList.extract(r);
            if (t == null) {
                com.antoshkaplus.words.model.Translation rt = repo.getTranslation(new TranslationKey(r.getForeignWord(), r.getNativeWord()));
                if (rt == null) {
                    // would have to create new
                    repo.addTranslation(toLocalTranslation(r));
                } else {
                    // don't forget to udpate id!!!!
                    repo.updateTranslation(toLocalTranslation(r));
                }
            } else {
                // put t to remote
                if (t.updateDate.after(r.getUpdateDate())) {
                    if (!t.creationDate.equals(r.getCreationDate())) {
                        t.creationDate = min(r.getCreationDate(), t.creationDate);

                        mergedLocalList.add(t);
                    }
                    mergedRemoteList.add(t);
                } else {
                    if (!t.creationDate.equals(r.getCreationDate())) {
                        r.setCreationDate(min(r.getCreationDate(), t.creationDate));
                        mergedRemoteList.add(r);
                    }
                    mergedLocalList.add(r);
                }
            }
        }
        for (com.antoshkaplus.words.model.Translation t : localList.getList()) {
            mergedRemoteList.add(t);
        }

        repo
        repo.updateTranslation();
    }

    private boolean after() {

    }


    private com.antoshkaplus.words.model.Translation toLocalTranslation(Translation tr) {
        com.antoshkaplus.words.model.Translation modelTr = new com.antoshkaplus.words.model.Translation(
                tr.getForeignWord(), tr.getNativeWord(), new Date(tr.getCreationDate().getValue()));
        return modelTr;
    }

    private Map<String, com.antoshkaplus.words.model.Translation> toKeyMap(List<com.antoshkaplus.words.model.Translation> list) {
        Collections.binarySearch()
        Map<String, com.antoshkaplus.words.model.Translation> map = new HashMap<>(list.size());
        for (com.antoshkaplus.words.model.Translation t : list) {
            map.put(t.)
        }

    }
}
