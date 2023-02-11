package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.databinding.FragmentQuizmeBinding;
import com.example.smartflashcards.dialogs.DialogData;

public class QuizMeFragment extends Fragment {

    private FragmentQuizmeBinding binding;
    private CardStackViewModel cardStackViewModel;
    private QuizmeViewModel mViewModel;

    private TextView textStackName;
    private TextView textQuestion;
    private EditText editResponse;

    private String question = "";

    //these variables are here so that they work in foreach
    private Boolean iterationBoolean = false;
    private String iterationString = "";

    public static QuizMeFragment newInstance() {
        return new QuizMeFragment();
    }

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuizMeFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem editQuestion = menu.add(R.string.action_edit_question);
        MenuItem reviewQuizStack = menu.add(R.string.action_review_quiz_stack);
        MenuItem randomizeQuizStack = menu.add(R.string.action_randomize);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle();
        if (nonNull(title)) { //check for null because back button also calls this and has no title
            /**
             * OPTION EDIT QUESTION
             */
            if (title.equals(getString(R.string.action_edit_question))) {
                if (nonNull(question)) {
                    cardStackViewModel.createModifyQuestionDialog(question, 0);
                };
                return true;
            }
            /**
             * OPTION REVIEW QUIZ STACK
             */
            if (title.equals(getString(R.string.action_review_quiz_stack))) {
                NavHostFragment.findNavController(QuizMeFragment.this)
                        .navigate(R.id.action_quizmeFragment_to_quizStackViewFragment);
                return true;
            }
            /**
             * OPTION RANDOMIZE QUIZ STACK
             */
            if (title.equals(getString(R.string.action_randomize))) {
                cardStackViewModel.buildRandomizedQuizStack();
                return true;
            }
        }
        return false;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = FragmentQuizmeBinding.inflate(inflater, container, false);
        View view = this.binding.getRoot();

        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.textStackName = (TextView) view.findViewById(R.id.stack_name);
        this.textQuestion = (TextView) view.findViewById(R.id.quiz_question);
        this.editResponse = view.findViewById(R.id.textInputResponse);

        this.textStackName.setText(this.cardStackViewModel.getStackName().getValue());

        this.cardStackViewModel.getQuizMeCard().observe(getViewLifecycleOwner(), quizCard -> {
            this.editResponse.setText("");
            if (nonNull(quizCard)) {
                this.editResponse.setFocusable(true);
                question = quizCard.getCardText();
                String prompt = this.cardStackViewModel.getStackDetails().getValue().getQuestionPrefix();
                prompt += this.question;
                prompt += this.cardStackViewModel.getStackDetails().getValue().getQuestionPostfix();
                this.textQuestion.setText(prompt);
            } else {
                this.textQuestion.setText(R.string.quizme_default);
                this.editResponse.setFocusable(false);
            }
        });

        this.editResponse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkResponse();
                    handled = true;
                }
                return handled;
            }
        });

        /**
         * SUBMIT RESPONSE BUTTON
         */
        binding.buttonSubmitResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkResponse();
            }
        });

        /**
         * ADD BUTTON
         */
        binding.addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackViewModel.createNewFlashcardDialog(null);
            }
        });

        /**
         * FORCE CARD REVIEW
         */
        this.cardStackViewModel.getForceCardReview().observe(getViewLifecycleOwner(), goReview -> {
            if (goReview) {
                NavHostFragment.findNavController(QuizMeFragment.this)
                        .navigate(R.id.action_quizmeFragment_to_cardEditorFragment);
                this.cardStackViewModel.clearForceCardReview();
            }
        });

        /**
         * BEGIN
         */
        this.cardStackViewModel.updateQuizMeCard();
        this.editResponse.requestFocus();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(QuizmeViewModel.class);
        // TODO: Use the ViewModel
    }

    private void checkResponse () {
        String response = editResponse.getText().toString().trim();
        QuestionCard questionCard = cardStackViewModel.findQuestionCard(question);

        if (nonNull(questionCard) && nonNull(response) && !response.equals("")) {

            //TODO: plan is to create temporary jeopardy cards whenever this indicates the need,
            // but CONSIDER THIS: if the user keeps missing the answer and the real card comes back up,
            //                    a duplicate jeopardy card can be created
            // Create jeopardy quizCard with node ID matched to the answerCard
            // (and make sure answer card w/ matching ID exists when card comes up)
            //this.answerInJeopardy = -1; //no answer in jeopardy
            // check other answers for jeopardy
            //for (int jIndex = 0; jIndex < this.numberAnswers; jIndex++) {
            //    if (this.correctAnswerCount[jIndex] * jeopardyMultiplier < this.correctAnswerCount[index]) {
            //        this.answerInJeopardy = jIndex;
            //    }
            //}


            iterationBoolean = false; //could use hash contains method, but need to loop through them anyway...
            iterationString = "";
            questionCard.getAnswers().forEach((key, rawAnswer) -> {
                String answer = (String) rawAnswer;
                answer = answer.trim();
                //TODO: ignore case? sometimes? just add case variations as other valid answers?
                if (response.equals(answer)) {
                    iterationBoolean = true;
                    cardStackViewModel.getQuizMeCard().getValue().recordCorrectResponse((int) key);
                } else {
                    iterationString += "\n   " + answer;
                }
            });
            String result;
            int numberAnswers = questionCard.getAnswers().size();
            int newPlacement = this.cardStackViewModel.getQuizMeCard().getValue().getLastPlacement();
            if (iterationBoolean) {
                newPlacement = newPlacement * 4; //TODO: Perhaps change to exponent instead of multiplier; either way, drive the number statistically for each user
                if (newPlacement == 0) {
                    newPlacement = 10; // TODO: change to learned value
                }
                result = getString(R.string.right_answer);
                if (numberAnswers > 1) {
                    result += "\n--------------\nAdditional correct answer";
                    result += (numberAnswers > 2) ? "s:" : ":";
                }
            } else {
                newPlacement = newPlacement / 2; //TODO: statistically drive this number for each user (also may need different formula for first time)
                result = getString(R.string.wrong_answer);
                result += "\n--------------\nThe ";
                result += this.cardStackViewModel.getStackDetails().getValue().getQuestionLabel();
                result += " was: " + this.cardStackViewModel.getStackDetails().getValue().getQuestionPrefix();
                result += this.question;
                result += this.cardStackViewModel.getStackDetails().getValue().getQuestionPostfix();
                result += "\n\nYour response was: ";
                result += response;
                result += "\n\n--------------\nCorrect answer";
                result += (numberAnswers > 1) ? "s:" : ":";
            }
            int finalPlacement = this.cardStackViewModel.moveQuizMeCard(newPlacement);

            //TODO: change message to "Moved to position " ##
            String message = result + iterationString + "\n--------------\nPlacement try = " + newPlacement + "; actual = " + finalPlacement;
            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
            dialogData.setMessage(message);
            this.cardStackViewModel.setDialogData(dialogData);

            //TODO: add to dialog:
            // provide option editing the answers,
            // if wrong, provide any questions that match the answer given,
            // add checkbox for the user to proclaim himself correct (Require, edit answers button)

        }
    }
}