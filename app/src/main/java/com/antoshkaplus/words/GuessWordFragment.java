package com.antoshkaplus.words;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GuessWordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GuessWordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GuessWordFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private ListView guesses;
    private TextView word;

    private GuessWordGame game;
    private TextToSpeech textToSpeech;

    private boolean gameOver = true;

    private Handler handler = new Handler();

    private Runnable nextGameEvent = null;

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
        v.setOnClickListener(this);
        guesses = (ListView)v.findViewById(R.id.guesses);
        guesses.setOnItemClickListener(this);
        guesses.setOnItemLongClickListener(this);
        word = (TextView)v.findViewById(R.id.word);
        if (game != null) {
            fillViews();
        }
        word.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
            game.switchGameType();
            OnNext(this);
        }
        return super.onOptionsItemSelected(item);
    }

    void setGame(GuessWordGame game) {
        gameOver = false;
        this.game = game;
        if (guesses != null) {
            fillViews();
        }
    }

    void fillViews() {
        guesses.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, game.getGuesses()));
        word.setText(game.getWord());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (gameOver) {
            OnNext(this);
            return;
        }
        gameOver = true;
        int correctPosition = game.getCorrectPosition();
        View correctView = guesses.getChildAt(correctPosition);
        correctView.setBackgroundColor(Color.GREEN);
        if (game.IsCorrect(position)) {
            OnCorrectGuess(this);
        } else {
            view.setBackgroundColor(Color.RED);
            OnIncorrectGuess(this);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        speak(game.getGuesses().get(position));
        return true;
    }

    private void speak(String word) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onClick(View view) {
        OnNext(this);
    }



    public void OnCorrectGuess(final GuessWordFragment fragment) {
        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext(fragment);
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    public void OnIncorrectGuess(final GuessWordFragment fragment) {
        nextGameEvent = new Runnable() {
            @Override
            public void run() {
                OnNext(fragment);
            }
        };
        handler.postDelayed(nextGameEvent, 2000);
    }

    public void OnNext(GuessWordFragment fragment) {
        handler.removeCallbacks(nextGameEvent);
        game.NewGame();
        fragment.setGame(game);
    }



    public interface OnFragmentInteractionListener {
    }

}
