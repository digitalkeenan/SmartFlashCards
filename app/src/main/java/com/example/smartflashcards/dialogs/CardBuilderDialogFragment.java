package com.example.smartflashcards.dialogs;

import static java.util.Objects.nonNull;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartflashcards.cards.AnswerCard;
import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.R;
import com.google.android.material.textfield.TextInputLayout;

public class CardBuilderDialogFragment extends DialogStackFragment {

    private CardStackViewModel cardStackViewModel;

    private QuestionCard questionCard;
    private AnswerCard answerCard;

    EditText editQuestion;
    EditText editAnswer;
    EditText editPlacement;
    FrameLayout placementInputs;
    TextView placementPrompt;
    CheckBox checkBoxEndPlacement;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_card_builder_dialog, null);

        TextInputLayout questionLayout = view.findViewById(R.id.QuestionTextInputLayout);
        TextInputLayout answerLayout = view.findViewById(R.id.AnswerTextInputLayout);

        questionLayout.setHint(cardStackViewModel.getStackDetails().getValue().getQuestionLabel());
        answerLayout.setHint(cardStackViewModel.getStackDetails().getValue().getAnswerLabel());

        TextView message = view.findViewById(R.id.message);
        this.editQuestion = view.findViewById(R.id.textInputQuestion);
        this.editAnswer = view.findViewById(R.id.textInputAnswer);
        this.placementInputs = view.findViewById(R.id.placement_inputs);
        this.editPlacement = view.findViewById(R.id.numberInputPlacement);
        this.placementPrompt = view.findViewById(R.id.placement_prompt);
        this.checkBoxEndPlacement = view.findViewById(R.id.checkBoxEndPlacement);

        this.editQuestion.requestFocus();

        this.editQuestion.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save();
                    handled = true;
                }
                return handled;
            }
        });

        this.editAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save();
                    handled = true;
                }
                return handled;
            }
        });

        this.editPlacement.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save();
                    handled = true;
                }
                return handled;
            }
        });

        this.editPlacement.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (checkBoxEndPlacement.isChecked()) {
                        checkBoxEndPlacement.toggle();
                    }
                } else {
                    editPlacement.setText(String.valueOf(placement()));
                }
            }
        });

        this.checkBoxEndPlacement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxEndPlacement.isChecked()) {
                    editPlacement.setText("-1");
                } else {
                    editPlacement.setText("0");
                }
                editPlacement.clearFocus(); // remove focus so can detect if user clicks to edit it
            }
        });

        this.checkBoxEndPlacement.toggle();
        this.editPlacement.setText("-1");

        if (nonNull(super.dialogData)) {
            switch (super.dialogData.getAction()) {
                case addQuestionCard:
                    /**
                     * MAKE NEW CARD
                     */
                    message.setText(super.dialogData.getMessage());
                    if (nonNull(super.dialogData.getFlashCard())) { // comes in as null for normal card builder
                        setFixedAnswer(super.dialogData.getFlashCard().getCardText());
                    }
                    break;
                case addAnswerToQuestionCard:
                    /**
                     * ADD ANSWER
                     */
                    message.setText(super.dialogData.getMessage());
                    this.questionCard = (QuestionCard) super.dialogData.getFlashCard();
                    setFixedQuestion(this.questionCard.getCardText());
                    break;
                case modifyQuestionCard:
                    /**
                     * MODIFY QUESTION
                     */
                    this.questionCard = (QuestionCard) super.dialogData.getFlashCard();
                    setFixedAnswer(this.questionCard.getAnswersString());
                    Integer placement = super.dialogData.getInteger();
                    if (nonNull(placement)) {
                        // if placement provide (nonNull), lock it
                        this.editPlacement.setText(placement.toString());
                        this.editPlacement.setFocusable(false);
                    }
                    message.setText(super.dialogData.getMessage());
                    this.editQuestion.setText(this.questionCard.getCardText());
                    break;
                case modifyAnswerCardAll:
                    /**
                     * MODIFY ANSWER - ALL
                     */
                    this.answerCard = (AnswerCard) super.dialogData.getFlashCard();
                    setFixedQuestion(this.answerCard.getQuestionsString());
                    message.setText(super.dialogData.getMessage());
                    this.editAnswer.setText(this.answerCard.getCardText());
                    break;
                case modifyAnswerCardOne:
                    /**
                     * MODIFY ANSWER - ONE
                     */
                    this.questionCard = (QuestionCard) super.dialogData.getFlashCard();
                    setFixedQuestion(this.questionCard.getCardText());
                    message.setText(super.dialogData.getMessage());
                    this.editAnswer.setText(super.dialogData.getDataString());
                    break;
            }
        }

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.saveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        save();
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CardBuilderDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void setFixedQuestion(String question) {
        this.editQuestion.setText(question);
        this.editQuestion.setFocusable(false);
        this.editAnswer.requestFocus();
        this.editAnswer.selectAll();
        this.placementPrompt.setVisibility(View.INVISIBLE);
        this.placementInputs.setVisibility(View.INVISIBLE);
    }

    public void setFixedAnswer(String answer) {
        this.editAnswer.setText(answer);
        this.editAnswer.setFocusable(false);
        this.editQuestion.selectAll();
        this.editQuestion.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    private int placement() {
        String placementString = this.editPlacement.getText().toString();
        int placement = -1;
        if (nonNull(placementString) && !placementString.equals("")) {
            try {
                placement = Integer.parseInt(placementString);
            } catch (NumberFormatException e) {
                //for excessively large entry, no need to do anything - just keep the -1 default
            }
        }
        if (placement < 0) {
            placement = -1;
            if (!this.checkBoxEndPlacement.isChecked()) {
                this.checkBoxEndPlacement.toggle();
            }
        }
        return placement;
    }

    private void save() {
        String question = this.editQuestion.getText().toString();
        String answer = this.editAnswer.getText().toString();

        if (nonNull(super.dialogData)) {
            switch (super.dialogData.getAction()) {
                case addQuestionCard:
                    /**
                     * MAKE NEW CARD
                     */
                    if (nonNull(dialogData.getFlashCard())) {
                        //answer was already known to exist
                        this.cardStackViewModel.addQuestionCard(question, answer,
                                (AnswerCard) dialogData.getFlashCard(), placement());
                    } else {
                        this.cardStackViewModel.addQuestionCard(question, answer, null, placement());
                    }

                    break;
                case addAnswerToQuestionCard:
                    /**
                     * ADD ANSWER
                     */
                    this.cardStackViewModel.addAnswerToQuestionCard(answer, this.questionCard);
                    break;
                case modifyQuestionCard:
                    /**
                     * MODIFY QUESTION
                     */
                    this.cardStackViewModel.modifyQuestionCard(
                            super.dialogData.getFlashCard().getCardText(), question, placement());
                    break;
                case modifyAnswerCardAll:
                    /**
                     * MODIFY ANSWER - ALL
                     */
                    this.cardStackViewModel.modifyAnswerCard(this.answerCard, answer);
                    break;
                case modifyAnswerCardOne:
                    /**
                     * MODIFY ANSWER - ONE
                     */
                    this.cardStackViewModel.modifyAnswer(this.questionCard,
                            super.dialogData.getDataString(), answer);
                    break;
            }
        }
        dismiss();
    }
}