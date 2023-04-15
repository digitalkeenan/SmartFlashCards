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
import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.databinding.FragmentQuizmeBinding;
import com.example.smartflashcards.dialogs.DialogData;

import java.util.ArrayList;

public class QuizMeFragment extends Fragment {

    private FragmentQuizmeBinding binding;
    private CardStackViewModel cardStackViewModel;
    private QuizmeViewModel mViewModel;

    private TextView textStackName;
    private TextView textQuizStats;
    private TextView textCardStats;
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
        MenuItem calculateGrade = menu.add(R.string.action_calculate_grade);
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
             * CALCULATE GRADE
             */
            if (title.equals(getString(R.string.action_calculate_grade))) {
                ArrayList<Integer> stats = cardStackViewModel.getQuizStats();
                Integer card_count = cardStackViewModel.getQuizSize();
                Integer grade = stats.get(5) * 100; // A
                grade += stats.get(4) * 90;         // B
                grade += stats.get(3) * 80;         // C
                grade += stats.get(2) * 70;         // D
                grade = grade / card_count;
                String report = "Current score: " + grade + "%";
                report += "\n-------------------";
                report += "\nA cards: " + stats.get(5);
                report += "\nB cards: " + stats.get(4);
                report += "\nC cards: " + stats.get(3);
                report += "\nD cards: " + stats.get(2);
                report += "\nF cards: " + stats.get(1);
                report += "\nuntried cards: " + stats.get(0);
                report += "\n-------------------";
                report += "\ntotal cards: " + card_count;
                DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
                dialogData.setMessage(report);
                this.cardStackViewModel.setDialogData(dialogData);
            }
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
        this.textQuizStats = (TextView) view.findViewById(R.id.quiz_stats);
        this.textCardStats = (TextView) view.findViewById(R.id.card_stats);
        this.textQuestion = (TextView) view.findViewById(R.id.quiz_question);
        this.editResponse = view.findViewById(R.id.textInputResponse);

        this.textStackName.setText(this.cardStackViewModel.getStackName().getValue());

        this.cardStackViewModel.getQuizMeCard().observe(getViewLifecycleOwner(), quizCard -> {
            updateQuizStats();

            String card_stats = "";

            this.editResponse.setText("");
            if (nonNull(quizCard)) {
                // TODO: only show last_status in debug mode
                //       for public users, show grade for non-stars
                card_stats = "this card: ";
                int last_placement = quizCard.getLastPlacement();
                int last_status = quizCard.getStatusPosition();
                if (last_placement < -1) {
                    card_stats += "GOLD STAR (" + last_status + ")";
                } else if (last_placement < 0) {
                    card_stats += "SILVER STAR (" + last_status + ")";
                } else if ((last_placement == 0) & (last_status != 0)) {
                    card_stats += "NEW (" + last_status + ")";
                } else {
                    card_stats += "last position = " + last_placement;
                    if (last_placement != last_status) {
                        card_stats += " (status = " + last_status + ")";
                    }
                }
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

            this.textCardStats.setText(card_stats);
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
         * DEFER RESPONSE BUTTON
         */
        binding.buttonDefer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deferCard();
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

    private void updateQuizStats () {
        String stats = "cards: " + this.cardStackViewModel.getQuizSize();
        stats += "\nsilver stars: " + this.cardStackViewModel.getSilverStars();
        stats += "\ngold stars: " + this.cardStackViewModel.getGoldStars();
        this.textQuizStats.setText(stats);
    }

    private void deferCard () {
        int quizSize = this.cardStackViewModel.getQuizSize();
        int starIntrusionDepth = 10; // TODO: when the other uses of 10 are changed to be statistical, make this match
        int nonstarLimit = quizSize - this.cardStackViewModel.getSilverStars()
                - this.cardStackViewModel.getGoldStars();
        nonstarLimit = Math.max(nonstarLimit, 0) + starIntrusionDepth;
        QuizCard quizCard = this.cardStackViewModel.getQuizMeCard().getValue();
        int finalPlacement = this.cardStackViewModel.moveQuizCard(quizCard, nonstarLimit, false);
        String message = "Moved to ";
        if (finalPlacement < 0) {
            message += "last position";
        } else {
            message += "position " + finalPlacement;
        }

        DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
        dialogData.setMessage(message);
        this.cardStackViewModel.setDialogData(dialogData);
    }

    private void checkResponse () {
        String response = editResponse.getText().toString().trim();
        QuestionCard questionCard = cardStackViewModel.findQuestionCard(question);

        int starIntrusionDepth = 10; // TODO: when the other uses of 10 are changed to be statistical,
                                     //       make this match
        int correctMultiplier = 4; // TODO: Perhaps change to exponent instead of multiplier;
                                   //       either way, drive the number statistically for each user
        int missDivisor = 2;  //TODO: statistically drive this number for each user
                              //      also may need different formula for first time

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
            String result = "\n--------------\nThe ";
            result += this.cardStackViewModel.getStackDetails().getValue().getQuestionLabel();
            result += " was: " + this.cardStackViewModel.getStackDetails().getValue().getQuestionPrefix();
            result += this.question;
            result += this.cardStackViewModel.getStackDetails().getValue().getQuestionPostfix();
            result += "\n\nYour response was: ";
            result += response;
            int numberAnswers = questionCard.getAnswers().size();
            int quizSize = this.cardStackViewModel.getQuizSize();
            //TODO: add randomization to somewhat earlier (perhaps by multiplying with 0.9 to 1)
            // but be careful to not break silver/gold check
            int lastPlacement = this.cardStackViewModel.getQuizMeCard().getValue().getLastPlacement();
            int lastStatus = this.cardStackViewModel.getQuizMeCard().getValue().getStatusPosition();
            int nonstarLimit = quizSize - this.cardStackViewModel.getSilverStars()
                                        - this.cardStackViewModel.getGoldStars();
            nonstarLimit = Math.max(nonstarLimit, 0) + starIntrusionDepth;
            if (iterationBoolean) {
                // CORRECT ANSWER
                result += "\n--------------\n" + getString(R.string.right_answer);
                if (numberAnswers > 1) {
                    result += "\n--------------\nAdditional correct answer";
                    result += (numberAnswers > 2) ? "s:" : ":";
                }
                QuizCard card = this.cardStackViewModel.moveQuizMeCard(true, correctMultiplier, nonstarLimit);
                int newPlacement = card.getLastPlacement();
                int newStatus = card.getStatusPosition();
                result += iterationString + "\n--------------\n";
                if (newPlacement < -1) {
                    result += "GOLD STAR";
                    result += " (" + newStatus + ")";
                } else if (newPlacement < 0) {
                    result += "SILVER STAR";
                    result += " (" + newStatus + ")";
                    // TODO: only show position/placement info in debug mode
                    //       for public users, show new grade
                    if (newStatus >= quizSize) {
                        result += "\nPlaced at end";
                    } else {
                        result += "\nMoved to position" + newStatus;
                    }
                } else if (newStatus == newPlacement) {
                    result += "Moved to position " + newPlacement;
                } else {
                    result += "Status position: " + newStatus;
                    result += "\nActual placement: " + newPlacement;
                }
            } else {
                // INCORRECT ANSWER
                cardStackViewModel.getQuizMeCard().getValue().recordWrongResponse();
                QuizCard card = this.cardStackViewModel.moveQuizMeCard(false, missDivisor, nonstarLimit);
                int newPlacement = card.getLastPlacement();
                result += "\n--------------\n" + getString(R.string.wrong_answer);
                result += "\n--------------\nCorrect answer";
                result += (numberAnswers > 1) ? "s:" : ":";
                result += iterationString + "\n--------------\n";
                result += "Moved to position " + newPlacement;
            }
            result += "\n(previous: " + lastPlacement + "," + lastStatus + ")";

            updateQuizStats();

            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
            dialogData.setMessage(result);
            this.cardStackViewModel.setDialogData(dialogData);

            //TODO: add to dialog:
            // provide option editing the answers,
            // if wrong, provide any questions that match the answer given,
            // add checkbox for the user to proclaim himself correct (Require, edit answers button)

        }
    }
}