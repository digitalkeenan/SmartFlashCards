package com.example.smartflashcards.stackDetails;

import static java.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.example.smartflashcards.R;
import com.example.smartflashcards.databinding.FragmentStackDetailsBinding;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.File;
import java.io.IOException;

public class StackDetailsFragment extends Fragment {

    private StackDetailsViewModel stackDetailsViewModel;

    private FragmentStackDetailsBinding binding;

    File detailsFile = null;

    private TextView nameTextView;
    private EditText descriptionEditText;
    private EditText questionLabelEditText;
    private EditText questionPretextEditText;
    private EditText questionPosttextEditText;
    private EditText answerLabelEditText;
    private EditText jeopardyPretextEditText;
    private EditText jeopardyPosttextEditText;


    public StackDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.binding = FragmentStackDetailsBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.stackDetailsViewModel = new ViewModelProvider(requireActivity()).get(StackDetailsViewModel.class);

        this.nameTextView = view.findViewById(R.id.stackName);
        this.nameTextView.setText(this.stackDetailsViewModel.getStackName().getValue());

        File directory = new File(getActivity().getFilesDir(), getString(R.string.stack_directory));
        File stackFolder = new File(directory, this.stackDetailsViewModel.getStackName().getValue());
        this.detailsFile = new File(stackFolder, getString(R.string.stack_details_file));

        if (!nonNull(this.stackDetailsViewModel.getStackDetails().getValue())) {
            if (this.detailsFile.exists()) {
                MyFileInputStream inputStream;
                try {
                    inputStream = new MyFileInputStream(this.detailsFile);
                    this.stackDetailsViewModel.setStackDetails(new StackDetails(inputStream));
                    inputStream.close();
               } catch (Exception e) {
                    e.printStackTrace();
                    //TODO: tell user that something went wrong and exit this fragment (otherwise, null ref follows)
                }
            } else {
                //load defaults
                this.stackDetailsViewModel.setStackDetails(new StackDetails(getContext()));
            }
            //TODO: if answerFile exists, read the beginning of it to get the number of questions, else 0
        }

        this.descriptionEditText = view.findViewById(R.id.textInputDescription);
        this.descriptionEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getDescription());

        this.questionLabelEditText = view.findViewById(R.id.textInputQuestion);
        this.questionLabelEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getQuestionLabel());

        this.questionPretextEditText = view.findViewById(R.id.textInputQuestionPretext);
        this.questionPretextEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getQuestionPrefix());

        this.questionPosttextEditText = view.findViewById(R.id.textInputQuestionPosttext);
        this.questionPosttextEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getQuestionPostfix());

        this.answerLabelEditText = view.findViewById(R.id.textInputAnswer);
        this.answerLabelEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getAnswerLabel());

        this.jeopardyPretextEditText = view.findViewById(R.id.textInputJeopardyPretext);
        this.jeopardyPretextEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getJeopardyPrefix());

        this.jeopardyPosttextEditText = view.findViewById(R.id.textInputJeopardyPosttext);
        this.jeopardyPosttextEditText.setText(this.stackDetailsViewModel.getStackDetails().getValue().getJeopardyPostfix());

        generateIllustrations(view);

        //TODO: if updating on all the key-presses works well, remove this button
        this.binding.buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonview) {
                generateIllustrations(view);
            }
        });

        this.jeopardyPosttextEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    NavHostFragment.findNavController(StackDetailsFragment.this)
                            .navigate(R.id.action_stackDetailsFragment_pop);
                    handled = true;
                }
                return handled;
            }
        });

        /**
         * generate example on every key-press
         */
        this.questionLabelEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
        this.questionPretextEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
        this.questionPosttextEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
        this.answerLabelEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
        this.jeopardyPretextEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
        this.jeopardyPretextEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View myView, int i, KeyEvent keyEvent) {
                generateIllustrations(view);
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //save details to view model
        this.stackDetailsViewModel.getStackDetails().getValue().setDescription(this.descriptionEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setQuestionLabel(this.questionLabelEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setQuestionPrefix(this.questionPretextEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setQuestionPostfix(this.questionPosttextEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setAnswerLabel(this.answerLabelEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setJeopardyPrefix(this.jeopardyPretextEditText.getText().toString());
        this.stackDetailsViewModel.getStackDetails().getValue().setJeopardyPostfix(this.jeopardyPosttextEditText.getText().toString());

        //set save-trigger for Main Activity to save to disk and check against it's active stack
        this.stackDetailsViewModel.setStackDetailsUpdated(this.stackDetailsViewModel.getStackName().getValue());

        this.binding = null;
    }

    public void generateIllustrations (View view) {
        TextView resultsTextView;
        String result;

        resultsTextView = view.findViewById(R.id.stackQuestionResult);
        result = this.questionLabelEditText.getText().toString() + ": ";
        result += this.questionPretextEditText.getText().toString() + "question-text";
        result += this.questionPosttextEditText.getText().toString() + "\n";
        result += this.answerLabelEditText.getText().toString() +": answer-text\n";
        resultsTextView.setText(result);

        resultsTextView = view.findViewById(R.id.stackJeopardyResult);
        result = this.answerLabelEditText.getText().toString() + ": ";
        result += this.jeopardyPretextEditText.getText().toString() + "answer-text";
        result += this.jeopardyPosttextEditText.getText().toString() + "\n";
        result += this.questionLabelEditText.getText().toString() +": question-text\n";
        resultsTextView.setText(result);
    }
}