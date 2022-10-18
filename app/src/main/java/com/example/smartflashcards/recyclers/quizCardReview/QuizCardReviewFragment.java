package com.example.smartflashcards.recyclers.quizCardReview;

import static java.util.Objects.nonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.R;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.databinding.FragmentQuizCardReviewBinding;
import com.example.smartflashcards.databinding.FragmentStackViewListBinding;
import com.example.smartflashcards.recyclers.cardEditor.CardEditorRecyclerViewAdapter;

public class QuizCardReviewFragment extends Fragment {

    public RecyclerView recyclerView;
    private LinearLayoutManager recyclerManager;
    private QuizCardViewAdapter adapter;

    private CardStackViewModel cardStackViewModel;

    private MenuItem clearStatistics;

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuizCardReviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentQuizCardReviewBinding binding = FragmentQuizCardReviewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setAdapter(view);

        TextView questionLabelView = view.findViewById(R.id.questionLabel);
        TextView questionTextView = view.findViewById(R.id.questionTextView);
        TextView positionTextView = view.findViewById(R.id.positionTextView);
        TextView placementTextView = view.findViewById(R.id.placementTextView);
        TextView wrongResponsesTextView = view.findViewById(R.id.wrongResponsesTextView);

        questionLabelView.setText(this.cardStackViewModel.getStackDetails().getValue().getQuestionLabel());

        QuizCard quizCard = (QuizCard) this.cardStackViewModel.getSelectionCard();
        if (nonNull(quizCard)) {
            if (quizCard.isJeopardy()) {
                questionTextView.setText(R.string.jeopardy_card_label + quizCard.getCardText());
            } else {
                questionTextView.setText(quizCard.getCardText());
            }
            positionTextView.setText(this.cardStackViewModel.getSelectionPosition().toString());
            placementTextView.setText(String.valueOf(quizCard.getLastPlacement()));
            wrongResponsesTextView.setText(String.valueOf(quizCard.getNumberWrongResponses()));

            QuestionCard questionCard = this.cardStackViewModel.findQuestionCard(quizCard.getCardText());
            quizCard.getCorrectAnswerCount().forEach((key, count) -> {
                String answer = questionCard.getAnswers().get((Integer) key).toString();
                this.adapter.addData(answer, (Integer) count);
            });
        }

        return view;
    }

    public void setAdapter(View view) {
        View recyclerLayout = view.findViewById(R.id.answers);

        this.adapter = new QuizCardViewAdapter();

        // Set the adapter
        if (recyclerLayout instanceof RecyclerView) {
            Context context = recyclerLayout.getContext();
            this.recyclerManager = new LinearLayoutManager(context);
            this.recyclerView = (RecyclerView) recyclerLayout;
            this.recyclerView.setLayoutManager(this.recyclerManager);
            this.recyclerView.setAdapter(this.adapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.clearStatistics = menu.add(R.string.action_clear_statistics);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle();
        if (nonNull(title)) { //check for null because back button also calls this and has no title
            if (title.equals(clearStatistics.getTitle())) { //TODO: if this works, change all my options code to work this way
                // TODO: create dialog to ask if sure, then call quizCard.clearStatistics()
                //  also must add observer to call clearAnswers method in this fragment
                ((QuizCard) this.cardStackViewModel.getSelectionCard()).clearStatistics();
                clearAnswers();
                return true;
            }
        }
        return false;
    }

    // TODO: are these both still needed with no selection?
    public Object getItemKey(int position) {
        return (long) position;
    }
    public Integer getItemPosition(Object key) {
        if (nonNull(key)) {
            Long longKey = (long) key;
            return longKey.intValue();
        } else {
            return null;
        }
    }

    //TODO: still needed?
    final class MyKeyProvider extends ItemKeyProvider {

        public MyKeyProvider(int scope) {
            super(scope);
        }

        @Nullable
        @Override
        public Object getKey(int position) {
            return getItemKey(position);
        }

        @Override
        public int getPosition(@NonNull Object key) {
            Integer position = getItemPosition(key);
            if (nonNull(position)) {
                return position;
            }
            return -1; // On notifyItemRemoved, it still tries to check for the item's position
        }
    }

    final class MyDetailsLookup extends ItemDetailsLookup {

        private final RecyclerView mRecyclerView;

        MyDetailsLookup(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        public @Nullable
        ItemDetails getItemDetails(@NonNull MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                if (holder instanceof QuizCardViewAdapter.ViewHolder) {
                    return ((QuizCardViewAdapter.ViewHolder)holder).getItemDetails();
                }
            }
            return null;
        }
    }


    /**
     * CHANGE NOTIFICATIONS
     */
    // this stuff will run on the UI (user interface) thread
    // this is necessary to avoid crashing into the layout manager
    // else, find some other way to wait for (!recyclerView.isComputingLayout && recyclerView.scrollState == SCROLL_STATE_IDLE)

    @SuppressLint("NotifyDataSetChanged")
    public void clearAnswers() {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.clearData();
                adapter.notifyDataSetChanged();
            }
        });
    }
}