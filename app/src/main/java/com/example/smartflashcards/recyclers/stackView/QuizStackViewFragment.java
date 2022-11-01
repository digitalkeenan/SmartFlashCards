package com.example.smartflashcards.recyclers.stackView;

import static java.util.Objects.nonNull;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartflashcards.FlashcardViewFilter;
import com.example.smartflashcards.R;
import com.example.smartflashcards.cardTrees.CardTreeNode;

public class QuizStackViewFragment extends StackViewFragment {

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuizStackViewFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.cardStackViewModel.setStackType(FlashcardViewFilter.StackType.QUIZ);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem deleteSelection = menu.add(R.string.action_delete_selection);
        MenuItem editSelection = menu.add(R.string.action_edit_selection);
        MenuItem reviewSelection = menu.add(R.string.action_review_selection);
        MenuItem randomize = menu.add(R.string.action_randomize);
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
                    super.cardStackViewModel.createDeleteQuestionDialog(selectionString);
                }
                return true;
            }
            /**
             * OPTION EDIT SELECTION
             */
            if (title.equals(getString(R.string.action_edit_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    super.cardStackViewModel.createModifyQuestionDialog(selectionString, super.cardStackViewModel.getSelectionStackPosition());
                };
                return true;
            }
            /**
             * OPTION REVIEW SELECTION
             */
            if (title.equals(getString(R.string.action_review_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    NavHostFragment.findNavController(QuizStackViewFragment.this)
                            .navigate(R.id.action_quizStackViewFragment_to_quizCardReviewFragment);
                }
                return true;
            }
            /**
             * OPTION RANDOMIZE
             */
            if (title.equals(getString(R.string.action_randomize))) {
                //TODO: add dialog warning user that
                // all correct/incorrect answer and stack placement statistics will be discarded
                cardStackViewModel.buildRandomizedQuizStack();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
             * HIDE VIEW SWITCH TODO: perhaps use different layout w/o this & add column w/ position #
             */
        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);
        toggleButton.setVisibility(View.INVISIBLE);
        TextView switchPrompt = view.findViewById(R.id.switchPromptTextView);
        switchPrompt.setVisibility(View.INVISIBLE);


        /**
         * OBSERVE FORCE-CARD-REVIEW
         */
        super.cardStackViewModel.getForceCardReview().observe(getViewLifecycleOwner(), goReview -> {
            if (goReview) {
                NavHostFragment.findNavController(QuizStackViewFragment.this)
                        .navigate(R.id.action_quizStackViewFragment_to_cardEditorFragment);
                cardStackViewModel.clearForceCardReview();
            }
        });

        /**
         * OBSERVE INVALID QUIZ CARDS
         */
        super.cardStackViewModel.getTopOfInvalidQuizNodeStack().observe(getViewLifecycleOwner(), invalidNode -> {
            if (nonNull(invalidNode)) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        callDeleteQuizCard(invalidNode);
                    }
                });
            }
        });
    }

    // Wait for initial selection or prior delete to complete
    private void callDeleteQuizCard(CardTreeNode invalidNode) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                deleteQuizCard(invalidNode);
            }
        });
    }
    private void deleteQuizCard(CardTreeNode invalidNode) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                /* NOTE: further waiting between the following commands would be needed
                         if selection position could change. However, passing through
                         quizMeFragment to get to this one ensures that the first card
                         is valid, and selection is set there before this clean-up */
                cardStackViewModel.deleteQuizCard(invalidNode);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        cardStackViewModel.popInvalidQuizCardStack();
                    }
                });
            }
        });
    }
}