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
public class GuessWordFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView guesses;
    private TextView word;

    private GuessWordGame game;
    private TextToSpeech textToSpeech;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GuessWordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GuessWordFragment newInstance(String param1, String param2) {
        GuessWordFragment fragment = new GuessWordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GuessWordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_guess_word, container, false);
        guesses = (ListView)v.findViewById(R.id.guesses);
        guesses.setOnItemClickListener(this);
        guesses.setOnItemLongClickListener(this);
        word = (TextView)v.findViewById(R.id.word);
        word.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(game.getWord().word);
                return true;
            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

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
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void setGame(GuessWordGame game) {
        this.game = game;

        guesses.setAdapter(new ArrayAdapter<Word>(getActivity(), android.R.layout.simple_list_item_1, game.getGuesses()));
        word.setText(game.getWord().word);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (game.IsCorrect(position)) {
            // should myself color green or red depending on result
            view.setBackgroundColor(Color.GREEN);
            // send result to activity
            mListener.OnCorrectGuess(this);

        } else {
            view.setBackgroundColor(Color.RED);
            // send result to activity
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


    public interface OnFragmentInteractionListener {

        public void OnCorrectGuess(GuessWordFragment fragment);
        public void OnIncorrectGuess(GuessWordFragment fragment);

    }

}
