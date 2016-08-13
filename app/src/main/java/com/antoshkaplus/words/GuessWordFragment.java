package com.antoshkaplus.words;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.list.CursorableLinkedList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GuessWordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GuessWordFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * functionality:
 * show history of played games
 *
 * testing:
 * try to play with play capacity equal to 1
 */
public class GuessWordFragment extends Fragment implements
        View.OnClickListener {

    private static final int PLAYS_CAPACITY = 20;
    private static final int GUESS_COUNT = 4;

    private OnFragmentInteractionListener mListener;

    private List<TextView> guesses;
    private TextView word;

    private GuessWordGameFactory factory;

    private TextToSpeech textToSpeech;

    private Handler handler = new Handler();
    private Runnable nextGameEvent = null;
    private TranslationRepository repo;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBackPressed();
        }
    };

    Plays plays = new Plays(PLAYS_CAPACITY);


    public static GuessWordFragment newInstance() {
        GuessWordFragment fragment = new GuessWordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_guess_word, container, false);
        //v.setOnClickListener(this);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.guesses);
        layout.setOnClickListener(this);
        guesses = new ArrayList<>();
        for (int i = 0; i < GUESS_COUNT; ++i) {
            TextView tv = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, layout, false);
            GuessListener lis = new GuessListener(i);
            tv.setOnClickListener(lis);
            tv.setOnLongClickListener(lis);
            layout.addView(tv);
            guesses.add(tv);
        }
        word = (TextView)v.findViewById(R.id.word);
        word.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        repo = new TranslationRepository(activity);
        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
        try {
            factory = new GuessWordGameFactory(repo.getAllTranslations());
            factory.setGuessCount(GUESS_COUNT);

            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_guess_word, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_switch_game_type);
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_switch_game_type) {
            factory.switchGameType();
            OnNext();
        }
        return super.onOptionsItemSelected(item);
    }

    void fillViews(GuessWordGame game) {
        List<String> textGuesses = game.getGuesses();
        for (int i = 0; i < GUESS_COUNT; ++i) {
            guesses.get(i).setBackgroundColor(Color.WHITE);
            guesses.get(i).setText(textGuesses.get(i));
        }
        word.setText(game.getWord());
    }

    void fillOutcomes(Play play) {
        int correctPosition = play.game.getCorrectPosition();
        guesses.get(correctPosition).setBackgroundColor(Color.GREEN);

        if (!play.game.IsCorrect(play.chosenPosition)) {
            guesses.get(play.chosenPosition).setBackgroundColor(Color.RED);
        }
    }

    void fillCurrent() {
        Play play = plays.getCurrent();
        fillViews(play.game);
        if (play.isFinished()) {
            fillOutcomes(play);
        }
    }

    private void speak(String word) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onClick(View view) {
        if (!plays.hasNext()) {
            if (plays.getCurrent().isFinished()) {
                OnNext();
            } else {
                Toast.makeText(getContext(), R.string.toast__play_current, Toast.LENGTH_SHORT).show();
            }
        } else {
            plays.moveNext();
            fillCurrent();
        }
    }

    public void OnCorrectGuess(GuessWordGame game) {
        try {
            repo.increaseSuccessScore(game.getForeignWord(game.getCorrectPosition()), 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext();
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    public void OnIncorrectGuess(GuessWordGame game, int chosenPosition) {
        try {
            repo.increaseFailureScore(game.getForeignWord(game.getCorrectPosition()), 1);
            repo.increaseFailureScore(game.getForeignWord(chosenPosition), 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext();
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    public void OnNext() {
        handler.removeCallbacks(nextGameEvent);
        GuessWordGame game = factory.createNew();
        plays.add(new Play(game));
        plays.moveNext();
        fillViews(game);
    }

    @Override
    public void onStart() {
        super.onStart();
        OnNext();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,
                new IntentFilter(BackPressedReceiver.ACTION_BACK_PRESSED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    public void onBackPressed() {
        if (!plays.hasPrev()) {
            Toast.makeText(getContext(), R.string.toast__no_previous_games, Toast.LENGTH_SHORT).show();
        } else {
            plays.movePrev();
            fillCurrent();
        }
    }



    public interface OnFragmentInteractionListener {
    }

    class Play {
        GuessWordGame game;
        int chosenPosition = -1;

        Play(GuessWordGame game) {
            this.game = game;
        }

        Play(GuessWordGame game, int chosenPosition) {
            this.game = game;
            this.chosenPosition = chosenPosition;
        }

        boolean isFinished() {
            return chosenPosition >= 0;
        }
    }

    
    static class Plays {

        LinkedList<Play> prev = new LinkedList<>();
        LinkedList<Play> next = new LinkedList<>();
        Play current;

        int capacity;

        Plays(int capacity) {
            this.capacity = capacity;
        }

        Play getCurrent() {
            return current;
        }

        int size() {
            if (current == null) return 0;
            return 1 + prev.size() + next.size();
        }

        void add(Play play) {
            if (capacity <= size() && !prev.isEmpty()) {
                prev.removeFirst();
            }
            next.addLast(play);
        }

        Play moveNext() {
            if (current != null) {
                prev.addLast(current);
            }
            return current = next.removeFirst();
        }

        Play movePrev() {
            if (current != null) {
                next.addFirst(current);
            }
            return current = prev.removeLast();
        }

        boolean hasNext() {
            return !next.isEmpty();
        }

        boolean hasPrev() {
            return !prev.isEmpty();
        }
    }

    class GuessListener implements View.OnClickListener, View.OnLongClickListener {

        int guessIndex;

        GuessListener(int guessIndex) {
            this.guessIndex = guessIndex;
        }

        @Override
        public void onClick(View v) {
            if (plays.hasNext()) {
                plays.moveNext();
                fillCurrent();
                return;
            }
            // latest
            Play play = plays.getCurrent();
            if (play.isFinished()) {
                OnNext();
                return;
            }
            GuessWordGame game = play.game;
            play.chosenPosition = guessIndex;
            fillOutcomes(play);
            // should just call callback with position.
            // but two of them separate a lot more
            if (game.IsCorrect(guessIndex)) {
                OnCorrectGuess(game);
            } else {
                OnIncorrectGuess(game, guessIndex);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            String s = plays.getCurrent().game.getGuesses().get(guessIndex);
            speak(s);
            return true;
        }
    }


}
