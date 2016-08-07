package com.antoshkaplus.words;

import com.antoshkaplus.words.backend.dictionaryApi.model.Translation;
import com.antoshkaplus.words.backend.dictionaryApi.model.TranslationList;
import com.antoshkaplus.words.model.TranslationKey;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Iterables;

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
 *
 * you have to sets of translations:
 * remoteList : new from the server
 * localList : new from local db by timestamp from lastSync
 *      note: local db watches for updatedDate to be more or equal to timestamp
 *
 * they intersect each other.
 *
 * the goal is to create two sets:
 * remoteUpdates
 * localUpdates
 *
 * both have updated items and new items from another set
 *
 * localUpdates should be pushed to local db right away
 * and remoteUpdates go out
 *
 */

public class TranslationMerger {

    private TranslationRepository repo;
    private Updates updates = new Updates();

    private static class Updates {
        List<Translation> remote = new ArrayList<>();
        // local and remote intersection
        List<com.antoshkaplus.words.model.Translation> localIntersection = new ArrayList<>();
        // remotes not found in locals
        List<com.antoshkaplus.words.model.Translation> localNotFound = new ArrayList<>();
        // new local (not present in remote but may be on server)
        List<com.antoshkaplus.words.model.Translation> localNew = new ArrayList<>();
    }

    // using this class to avoid explicit transformation from remote to local in code
    private static class LocalTranslationList {

        List<com.antoshkaplus.words.model.Translation> translationList;
        LocalTranslationComparator comparator;

        static class LocalTranslationComparator implements Comparator<com.antoshkaplus.words.model.Translation> {
            @Override
            public int compare(com.antoshkaplus.words.model.Translation t_0,
                               com.antoshkaplus.words.model.Translation t_1) {

                int comp = t_0.foreignWord.compareTo(t_1.foreignWord);
                if (comp == 0) {
                    comp = t_0.nativeWord.compareTo(t_1.nativeWord);
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


    public List<Translation> mergeRemote(final List<Translation> remoteList) throws Exception {
        // merging updates from server and database in one transaction
        this.updates = repo.executeBatch(new Callable<Updates>() {
            @Override
            public Updates call() throws Exception {
                Updates updates = computeUpdates(repo.getSyncedTranslationList(false), remoteList);

                for (com.antoshkaplus.words.model.Translation t : updates.localIntersection) {
                    repo.updateTranslation(t);
                }
                for (com.antoshkaplus.words.model.Translation t : updates.localNotFound) {
                    TranslationKey key = new TranslationKey(t.foreignWord, t.nativeWord);
                    com.antoshkaplus.words.model.Translation tKey = repo.getTranslation(key);
                    if (tKey == null) {
                        repo.addTranslation(t);
                    } else {
                        t.id = tKey.id;
                        repo.updateTranslation(t);
                    }
                }
                // can be done only after successful push to server????
                for (com.antoshkaplus.words.model.Translation t : updates.localNew) {
                    repo.updateTranslation(t);
                }
                return updates;
            }
        });
        return updates.remote;
    }

    // syncing updates
    void onRemoteUpdateSuccess() throws Exception {
        Iterable<com.antoshkaplus.words.model.Translation> it = Iterables.concat(
                updates.localNew, updates.localNotFound, updates.localIntersection);
        for (com.antoshkaplus.words.model.Translation t :  it) {
            repo.trySyncTranslation(t);
        }
    }

    private Updates computeUpdates(List<com.antoshkaplus.words.model.Translation> local,
                                   List<Translation> remote) {

        Updates updates = new Updates();
        LocalTranslationList localList = new LocalTranslationList(local);

        for (Translation r : remote) {
            com.antoshkaplus.words.model.Translation t = localList.extract(r);
            if (t == null) {
                updates.localNotFound.add(toLocal(r));
            } else {
                long t_creation = t.creationDate.getTime();
                long r_creation = r.getCreationDate().getValue();
                boolean creationEqual = t_creation == r_creation;
                long earliestCreation = Math.min(t_creation, r_creation);
                if (updateDateLess(t, r)) {
                    // r is newer
                    if (!creationEqual) {
                        r.setCreationDate(new DateTime(earliestCreation));
                        // we are fine sending something with
                        // same update date
                        updates.remote.add(r);
                    }
                    com.antoshkaplus.words.model.Translation tr = toLocal(r);
                    tr.id = t.id;
                    updates.localIntersection.add(tr);
                } else {
                    if (!creationEqual) {
                        t.creationDate = new Date(earliestCreation);
                        updates.localIntersection.add(t);
                    }
                    updates.remote.add(toRemote(t));
                }
            }
        }
        for (com.antoshkaplus.words.model.Translation t : localList.getList()) {
            updates.remote.add(toRemote(t));
        }
        updates.localNew = localList.getList();
        return updates;
    }


    private boolean updateDateLess(com.antoshkaplus.words.model.Translation t, Translation r) {
        return less(t.updateDate, r.getUpdateDate());
    }

    private boolean less(Date date, DateTime dateTime) {
        return date.getTime() < dateTime.getValue();
    }

    private com.antoshkaplus.words.model.Translation toLocal(Translation r) {
        com.antoshkaplus.words.model.Translation t = new com.antoshkaplus.words.model.Translation();
        t.creationDate = new Date(r.getCreationDate().getValue());
        t.updateDate = new Date(r.getUpdateDate().getValue());
        t.deleted = r.getDeleted();
        t.foreignWord = r.getForeignWord();
        t.nativeWord = r.getNativeWord();
        return t;
    }

    private Translation toRemote(com.antoshkaplus.words.model.Translation t) {
        Translation r = new Translation();
        r.setCreationDate(new DateTime(t.creationDate.getTime()));
        r.setUpdateDate(new DateTime(t.updateDate.getTime()));
        r.setDeleted(t.deleted);
        r.setForeignWord(t.foreignWord);
        r.setNativeWord(t.nativeWord);
        return r;
    }

}
