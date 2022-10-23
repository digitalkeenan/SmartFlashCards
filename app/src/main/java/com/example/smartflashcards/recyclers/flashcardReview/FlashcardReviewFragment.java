package com.example.smartflashcards.recyclers.flashcardReview;

import static java.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartflashcards.cards.AnswerCard;
import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.R;
import com.example.smartflashcards.databinding.FragmentCardEditorBinding;
import com.example.smartflashcards.dialogs.DialogData;
import com.example.smartflashcards.recyclers.recycler.RecyclerFragment;

public class FlashcardReviewFragment extends RecyclerFragment {

    private FragmentCardEditorBinding binding;

    private CardStackViewModel cardStackViewModel;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlashcardReviewFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        setAdapter(new FlashcardReviewRecyclerViewAdapter(cardStackViewModel));
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem deleteSelection = menu.add(R.string.action_delete_selection);
        MenuItem editSelection = menu.add(R.string.action_edit_selection);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle();
        if (nonNull(title)) { //check for null because back button also calls this and has no title
            if (title.equals(getString(R.string.action_delete_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    if (this.cardStackViewModel.getReviewCard() instanceof AnswerCard) {
                        if (((AnswerCard) this.cardStackViewModel.getReviewCard()).getNumberQuestions() > 1) {
                            String answer = this.cardStackViewModel.getReviewCard().getCardText();
                            QuestionCard questionCard = this.cardStackViewModel.findQuestionCard(selectionString);
                            this.cardStackViewModel.deleteAnswerFromQuestionCard(answer, questionCard, false);
                        } else {
                            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
                            String message = "Deleting the last " + this.cardStackViewModel.getStackDetails().getValue().getQuestionLabel();
                            message += " is not allowed\nYou may return to the " + getString(R.string.stack_view_fragment_label);
                            message += " screen and delete the entire " + this.cardStackViewModel.getStackDetails().getValue().getAnswerLabel();
                            dialogData.setMessage(message);
                            this.cardStackViewModel.setDialogData(dialogData);
                        }
                    } else {
                        if (((QuestionCard)this.cardStackViewModel.getReviewCard()).getAnswers().size() > 1) {
                            String answer = selectionString;
                            QuestionCard questionCard = (QuestionCard) this.cardStackViewModel.getReviewCard();
                            this.cardStackViewModel.deleteAnswerFromQuestionCard(answer, questionCard, true);
                        } else {
                            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
                            String message = "Deleting the last " + this.cardStackViewModel.getStackDetails().getValue().getAnswerLabel();
                            message += " is not allowed\nYou may return to the " + getString(R.string.stack_view_fragment_label);
                            message += " screen and delete the entire " + this.cardStackViewModel.getStackDetails().getValue().getQuestionLabel();
                            dialogData.setMessage(message);
                            this.cardStackViewModel.setDialogData(dialogData);
                        }
                    }
                };
                return true;
            }
            if (title.equals(getString(R.string.action_edit_selection))) {
                String selectionString = getSelectionString();
                if (nonNull(selectionString)) {
                    if (this.cardStackViewModel.getReviewCard() instanceof AnswerCard) {
                        // if view is answers, the selected item is a question linked from that answer view, and vice versa
                        cardStackViewModel.createModifyQuestionDialog(selectionString);
                    } else {
                        // here the answer will only be changed in the selected QuestionCard
                        cardStackViewModel.createModifyAnswerDialog(selectionString,
                                (QuestionCard) cardStackViewModel.getReviewCard());
                    }
                };
            }
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.binding = FragmentCardEditorBinding.inflate(inflater, container, false);
        View view = this.binding.getRoot();
        super.setAdapter(view);

        this.cardStackViewModel.clearCardNotifications();
        //set the card text in the editor screen
        if (nonNull(this.cardStackViewModel.getReviewCard())) {
            TextView textView = view.findViewById(R.id.header);
            String header = "";
            if (this.cardStackViewModel.getReviewCard() instanceof AnswerCard) {
                header += this.cardStackViewModel.getStackDetails().getValue().getJeopardyPrefix();
                header += this.cardStackViewModel.getReviewCard().getCardText();
                header += this.cardStackViewModel.getStackDetails().getValue().getJeopardyPostfix();
            } else {
                header += this.cardStackViewModel.getStackDetails().getValue().getQuestionPrefix();
                header += this.cardStackViewModel.getReviewCard().getCardText();
                header += this.cardStackViewModel.getStackDetails().getValue().getQuestionPostfix();
            }
            textView.setText(header);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * ADD BUTTON
         */
        this.binding.addStringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cardStackViewModel.getReviewCard() instanceof AnswerCard) {
                    cardStackViewModel.createNewFlashcardDialog(cardStackViewModel.getReviewCard());
                } else {
                    cardStackViewModel.createAddAnswerDialog((QuestionCard) cardStackViewModel.getReviewCard());
                }
            }
        });

        /**
         * OBSERVE NOTIFY CHANGES
         */
        this.cardStackViewModel.getCardSelectionChanged().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifySelectionChanged(position);
            }
        });
        this.cardStackViewModel.getCardNewItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifyItemInserted(position);
            }
        });
        this.cardStackViewModel.getCardDeletedItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifyItemRemoved(position, true);
            }
        });
        this.cardStackViewModel.getCardChangedItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifyItemChanged(position);
            }
        });
        this.cardStackViewModel.getCardMovedItem().observe(getViewLifecycleOwner(), oldPosition -> {
            if (nonNull(oldPosition)) {
                notifyItemMoved(oldPosition, this.cardStackViewModel.getCardNewPosition());
            }
        });

    }
}