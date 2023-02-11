package com.example.smartflashcards.recyclers.stackView;

import static java.util.Objects.nonNull;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartflashcards.FlashcardViewFilter;
import com.example.smartflashcards.R;
import com.example.smartflashcards.cardTrees.AlphabeticalCardTree;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AlphabeticalStackViewFragment extends StackViewFragment {

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlphabeticalStackViewFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure stackType is set to either question or answer
        if (super.cardStackViewModel.getStackType() != FlashcardViewFilter.StackType.ANSWER) {
            super.cardStackViewModel.setStackType(FlashcardViewFilter.StackType.QUESTION);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem deleteSelection = menu.add(R.string.action_delete_selection);
        MenuItem editSelection = menu.add(R.string.action_edit_selection);
        MenuItem reviewSelection = menu.add(R.string.action_review_selection);
        MenuItem importCards = menu.add(R.string.action_import_cards);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle();
        if (nonNull(title)) { //check for null because back button also calls this and has no title
            /**
             * OPTION DELETE SELECTION
             */
            if (title.equals(getString(R.string.action_delete_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    switch (cardStackViewModel.getStackType()) {
                        case ANSWER:
                            cardStackViewModel.deleteAnswer(selectionString);
                            break;
                        case QUESTION:
                            cardStackViewModel.createDeleteQuestionDialog(selectionString);
                            break;
                    }
                }
                return true;
            }
            /**
             * OPTION EDIT SELECTION
             */
            if (title.equals(getString(R.string.action_edit_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    switch (cardStackViewModel.getStackType()) {
                        case ANSWER:
                            cardStackViewModel.createModifyAnswerDialog(selectionString);
                            break;
                        case QUESTION:
                            cardStackViewModel.createModifyQuestionDialog(selectionString);
                            break;
                    }
                };
                return true;
            }
            /**
             * OPTION REVIEW SELECTION
             */
            if (title.equals(getString(R.string.action_review_selection))) {
                this.cardStackViewModel.setReviewCard(this.cardStackViewModel.getSelectionCard());
                if (nonNull(this.cardStackViewModel.getReviewCard())) {
                    NavHostFragment.findNavController(AlphabeticalStackViewFragment.this)
                            .navigate(R.id.action_alphabeticalStackViewFragment_to_cardEditorFragment);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * VIEW TOGGLE BUTTON
         */
        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);
        toggleButton.setTextOff(super.cardStackViewModel.getStackDetails().getValue().getQuestionLabel());
        toggleButton.setTextOn(super.cardStackViewModel.getStackDetails().getValue().getAnswerLabel());
        toggleButton.setChecked(super.cardStackViewModel.getStackType() == FlashcardViewFilter.StackType.ANSWER);
        super.binding.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.toggleButton.isChecked()) {
                    cardStackViewModel.setStackType(FlashcardViewFilter.StackType.ANSWER);
                } else {
                    cardStackViewModel.setStackType(FlashcardViewFilter.StackType.QUESTION);
                }
            }
        });

        /**
         * OBSERVE FORCE-CARD-REVIEW
         */
        cardStackViewModel.getForceCardReview().observe(getViewLifecycleOwner(), goReview -> {
            if (goReview) {
                NavHostFragment.findNavController(AlphabeticalStackViewFragment.this)
                        .navigate(R.id.action_alphabeticalStackViewFragment_to_cardEditorFragment);
                cardStackViewModel.clearForceCardReview();
            }
        });

    }
}