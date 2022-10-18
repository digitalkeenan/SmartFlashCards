package com.example.smartflashcards.recyclers.stackView;

import static java.util.Objects.nonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartflashcards.cardTrees.AlphabeticalCardTree;
import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.cardTrees.CardTreeNode;
import com.example.smartflashcards.FlashcardViewFilter;
import com.example.smartflashcards.R;
import com.example.smartflashcards.databinding.FragmentStackViewListBinding;
import com.example.smartflashcards.recyclers.recycler.RecyclerFragment;

public class StackViewFragment extends RecyclerFragment {

    protected CardStackViewModel cardStackViewModel;

    protected FragmentStackViewListBinding binding;

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StackViewFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setKeysTrackWithItems(true);
        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        setAdapter(new StackViewRecyclerViewAdapter(this.cardStackViewModel));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.binding = FragmentStackViewListBinding.inflate(inflater, container, false);
        View view = this.binding.getRoot();
        super.setAdapter(view);

        // clear when starting view, including when coming back from a subsequent navigation
        this.cardStackViewModel.clearStackNotifications();

        notifySelectionChanged(this.cardStackViewModel.getSelectionPosition());

        return view;
    }

    @Override
    public Object getItemKey(int position) {
        CardTreeNode itemKey = this.cardStackViewModel.getViewNode(position);
        return itemKey;
    }

    @Override
    public Integer getItemPosition(Object key) {
        CardTreeNode node = (CardTreeNode) key;
        Integer itemPosition = this.cardStackViewModel.getViewPosition(node);
        return itemPosition;
    }

    // Store selection position in view model
    // Handle click of "more" in filter mode
    @Override
    public Integer fixSelection(Integer newSelectionPosition) {
        Integer mySelectionPosition = super.fixSelection(newSelectionPosition);
        if (nonNull(mySelectionPosition)) {
            this.cardStackViewModel.setSelectionPosition(mySelectionPosition);
            if (this.cardStackViewModel.getFilterMode()) {
                int lastPosition = this.cardStackViewModel.getFilterList().size() - 1;
                if ((lastPosition >= 0)
                        && (mySelectionPosition == lastPosition)
                        && getItemString(lastPosition).equals(FlashcardViewFilter.more)) {
                    int itemsAdded = cardStackViewModel.addToFilterList(getVisibleItemCount());
                    if (itemsAdded < 0) {
                        // removed "more"
                        notifyItemRemoved(lastPosition, true);
                    } else {
                        if (itemsAdded > 0) {
                            notifyItemRangeInserted(lastPosition, itemsAdded);
                        }
                        String lastItem = this.cardStackViewModel.getFilterList().get(lastPosition + itemsAdded);
                        if (lastItem.equals(FlashcardViewFilter.more)) {
                            // selection is already on first new item
                        } else {
                            // replaced "more" with a real item
                            notifyItemChanged(lastPosition + itemsAdded);
                        }
                    }
                }
            }
        }
        return mySelectionPosition;
    }

    private void startFilter (String pattern) {
        if (nonNull(pattern)) {
            this.cardStackViewModel.startFilter(pattern, getVisibleItemCount());
            this.cardStackViewModel.setSelectionPosition(0);
            notifyDataSetChanged();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchText = view.findViewById(R.id.textInputSearch);

        /**
         * OBSERVE NOTIFY CHANGES
         */
        this.cardStackViewModel.getStackSwitched().observe(getViewLifecycleOwner(), switched -> {
            if (switched) {
                notifyDataSetChanged();
            }
        });
        this.cardStackViewModel.getStackSelectionChanged().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifySelectionChanged(position);
            }
        });
        this.cardStackViewModel.getStackNewItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifyItemInserted(position);
            }
        });
        this.cardStackViewModel.getStackDeletedItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                // Only adjust selection if the deleted item is the selected one
                // - this prevents selection updates during clean-up of invalid quizCards
                notifyItemRemoved(position, (this.cardStackViewModel.getSelectionPosition().equals(position)));
            }
        });
        this.cardStackViewModel.getStackChangedItem().observe(getViewLifecycleOwner(), position -> {
            if (nonNull(position)) {
                notifyItemChanged(position);
            }
        });
        this.cardStackViewModel.getStackMovedItem().observe(getViewLifecycleOwner(), oldPosition -> {
            if (nonNull(oldPosition)) {
                notifyItemMoved(oldPosition, this.cardStackViewModel.getStackNewPosition());
            }
        });

        /**
         * SEARCH BAR
         */
        ToggleButton filterButton = view.findViewById(R.id.filterButton);//TODO: is this needed? - perhaps to change button text, colors, etc.
        filterButton.setChecked(this.cardStackViewModel.getFilterMode());
        this.binding.filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the keyboard
                searchText.clearFocus();
                if (binding.filterButton.isChecked()) {
                    startFilter(searchText.getQuery().toString());
                } else {
                    Integer position = cardStackViewModel.getSelectionStackPosition();
                    cardStackViewModel.clearFilter();
                    notifyDataSetChanged();
                    // Set selection to same as before button was pressed
                    if (nonNull(position)) {
                        notifySelectionChanged(position);
                    }
                }
            }
        });

        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {//TODO: consider making ENTER button turn on the filter (and change the button to FILTER)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String pattern) {
                if (!pattern.equals(cardStackViewModel.getPattern())) {
                    if (cardStackViewModel.getFilterMode()) {
                        startFilter(pattern); //TODO: in view=quiz, force filter-mode on when text is entered (and off if "")
                   } else if (cardStackViewModel.getViewStack() instanceof AlphabeticalCardTree) {
                        Integer position = cardStackViewModel.findCardStartsWith(pattern);
                        if (nonNull(position)) {
                            notifySelectionChanged(position);
                        }
                    }
                }
                return false;
            }
        });
        // Initialize text to view model pattern
        String pattern = cardStackViewModel.getPattern();
        if (nonNull(pattern) && !pattern.equals("")) {
            searchText.setQuery(pattern, false);
            //searchText.requestFocus(); //TODO: why didn't this work? -- also changing to true above didn't help
        }


        /**
         * ADD BUTTON
         */
        this.binding.addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackViewModel.createNewFlashcardDialog(null);
            }
        });

    }
}