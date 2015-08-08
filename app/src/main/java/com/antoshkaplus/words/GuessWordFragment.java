package com.antoshkaplus.words;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.antoshkaplus.words.model.Word;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GuessWordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GuessWordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GuessWordFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private ListView guesses;
    private TextView word;

    private GuessWordGame game;
    private TextToSpeech textToSpeech;

    private boolean gameOver = true;

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
    public void onAttach(Activity activity) {
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

    void setGame(GuessWordGame game) {
        gameOver = false;
        this.game = game;
        if (guesses != null) {
            fillViews();
        }
    }

    void fillViews() {
        guesses.setAdapter(new ArrayAdapter<Word>(getActivity(), android.R.layout.simple_list_item_1, game.getGuesses()));
        word.setText(game.getWord().word);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (gameOver) {
            mListener.OnNext(this);
            return;
        }
        gameOver = true;
        int correctPosition = game.getCorrectPosition();
        View correctView = guesses.getChildAt(correctPosition);
        correctView.setBackgroundColor(Color.GREEN);
        if (game.IsCorrect(position)) {
            mListener.OnCorrectGuess(this);

        } else {
            view.setBackgroundColor(Color.RED);
            mListener.OnIncorrectGuess(this);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        speak(game.getGuesses().get(position).word);
        return true;
    }

    private void speak(String word) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onClick(View view) {
        mListener.OnNext(this);
    }


    public interface OnFragmentInteractionListener {

        void OnCorrectGuess(GuessWordFragment fragment);
        void OnIncorrectGuess(GuessWordFragment fragment);
        void OnNext(GuessWordFragment fragment);
    }

}
