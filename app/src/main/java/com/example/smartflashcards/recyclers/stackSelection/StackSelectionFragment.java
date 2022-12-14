package com.example.smartflashcards.recyclers.stackSelection;

import static java.util.Objects.nonNull;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.R;
import com.example.smartflashcards.databinding.FragmentStackSelectionListBinding;
import com.example.smartflashcards.stackDetails.StackDetailsViewModel;
import com.example.smartflashcards.dialogs.DialogData;

import java.io.File;

public class StackSelectionFragment extends Fragment {

    private FragmentStackSelectionListBinding binding;
    private LinearLayoutManager recyclerManager;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private File directory;

    private CardStackViewModel cardStackViewModel;
    private StackDetailsViewModel stackDetailsViewModel;

    private StackSelectionRecyclerViewAdapter adapter;
    private SelectionTracker<Long> tracker;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StackSelectionFragment() {
    }

    // TODO: Customize parameter initialization
    //  consider adding columns for size, date(s)
    @SuppressWarnings("unused")
    public static StackSelectionFragment newInstance(int columnCount) {
        StackSelectionFragment fragment = new StackSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        this.directory = new File(getActivity().getFilesDir(), getString(R.string.stack_directory));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem reviewSelection = menu.add(R.string.action_review_details);
        MenuItem deleteSelection = menu.add(R.string.action_delete_selection);
        //TODO: add renameSelection
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle(); //check for null because back button also calls this and has no title
        if (nonNull(title)) {
            /**
             * DELETE SELECTION
             */
            if (title.equals(getString(R.string.action_delete_selection))) {
                //this structure allows for multi-selection;
                this.tracker.getSelection().forEach(listItem -> {
                    String itemSelection = this.adapter.getString(listItem.intValue());
                    DialogData dialogData = new DialogData(DialogData.Type.CONTINUE_OR_CANCEL, DialogData.Action.deleteStack);
                    String message = "Are you sure that you want to delete the ";
                    message += itemSelection + " stack?";
                    dialogData.setMessage(message);
                    dialogData.setDataString(itemSelection);
                    this.cardStackViewModel.setDialogData(dialogData);
                });
                return true;
            }
            /**
             * REVIEW DETAILS
             */
            if (title.equals(getString(R.string.action_review_details))) {
                /**
                 * UPDATE stack-DETAILS-ViewModel
                 */
                this.tracker.getSelection().forEach(listItem -> {
                    String itemSelection = this.adapter.getString(listItem.intValue());
                    String stackName = this.stackDetailsViewModel.getStackName().getValue();
                    if (nonNull(stackName) && itemSelection.equals(stackName)) {
                        //if selected stack is already the one in the viewModel, do nothing
                    } else {
                        this.stackDetailsViewModel.selectStack(itemSelection);
                    }
                });
                NavHostFragment.findNavController(StackSelectionFragment.this)
                        .navigate(R.id.action_stackSelectionFragment_to_stackDetailsFragment);
                return true;
            }
        } else {
            //null title means back-button was pressed
            /**
             * UPDATE CARD-Stack-ViewModel
             */
            this.tracker.getSelection().forEach(listItem -> {
                String itemSelection = this.adapter.getString(listItem.intValue());
                this.stackDetailsViewModel.setStackSelection(itemSelection);
                String stackName = this.cardStackViewModel.getStackName().getValue();
                if (nonNull(stackName) && itemSelection.equals(stackName)) {
                    //if selected stack is already the one in the viewModel, do nothing
                } else {
                    this.cardStackViewModel.selectStack(itemSelection);
                }
            });
            return false; // not handling back-button navigation itself
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStackSelectionListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        stackDetailsViewModel = new ViewModelProvider(requireActivity()).get(StackDetailsViewModel.class);

        View recyclerLayout = view.findViewById(R.id.list);

        // Set the adapter
        if (recyclerLayout instanceof RecyclerView) {
            Context context = recyclerLayout.getContext();
            this.recyclerManager = new LinearLayoutManager(context);
            RecyclerView recyclerView = (RecyclerView) recyclerLayout;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(this.recyclerManager);
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            this.adapter = new StackSelectionRecyclerViewAdapter();
            this.adapter.updateData(this.directory);
            recyclerView.setAdapter(this.adapter);

            /*TODO: notes:
              started by following these examples:
              https://proandroiddev.com/a-guide-to-recyclerview-selection-3ed9f2381504
              https://medium.com/coding-blocks/implementing-selection-in-recyclerview-36a9739844e0
              https://www.raywenderlich.com/29024188-recyclerview-selection-library-tutorial-for-android-adding-new-actions
              https://stackoverflow.com/questions/55841912/how-to-implement-selectiontracker-in-java-not-kotlin
              HOWEVER, my first attempt didn't work (see https://stackoverflow.com/questions/73420737/why-isnt-onbindviewholder-getting-called-when-i-make-a-selection)
              Then I followed this example and it works (but I don't know what change fixed it):
              https://github.com/Thumar/recyclerview-selection/tree/master/app
            */
            this.tracker = new SelectionTracker.Builder<Long>(
                    "stack_selector",
                    recyclerView,
                    new StableIdKeyProvider(recyclerView),
                    new MyDetailsLookup(recyclerView),
                    StorageStrategy.createLongStorage()
                ).withSelectionPredicate(SelectionPredicates.<Long>createSelectSingleAnything()).build();
            adapter.injectTracker(this.tracker);

            SelectionTracker.SelectionObserver<Long> observer = new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    enforceMinimumSelection();
                }
            };
            this.tracker.addObserver(observer);

            enforceMinimumSelection();
        }
        return view;
    }

    private void makeSelection (String stackName) {
        for (int position = 0; position < adapter.getItemCount(); position++) {
            if (adapter.getString(position).equals(stackName)) {
                tracker.select((long) position);
                break;
            }
        }
    }

    private void enforceMinimumSelection() {
        //Check for de-selection - if so, select top item
        //This prevents the user from ever having to long-click
        final long[] foundSelection = {-1};
        tracker.getSelection().forEach(item -> {
            foundSelection[0] = item;
        });
        if (foundSelection[0] < 0) {
            if (adapter.getItemCount() > 0) {
                int topPosition = Math.max(0, this.recyclerManager.findFirstCompletelyVisibleItemPosition());
                tracker.select((long) topPosition);
            }
        } else if (foundSelection[0] > adapter.getItemCount()-1) {
            tracker.select(foundSelection[0]-1); // if selected item was last, select previous (new last)
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * ADD BUTTON
         */
        binding.addStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogData dialogData = new DialogData(DialogData.Type.CREATE_NEW_STACK, DialogData.Action.addStack);
                cardStackViewModel.setDialogData(dialogData);
            }
        });

        /**
         * OBSERVE STACK SELECTION
         */
        stackDetailsViewModel.getStackSelection().observe(getViewLifecycleOwner(), stackName -> {
            this.adapter.updateData(directory);
            makeSelection(stackName);
            enforceMinimumSelection();
        });

        /**
         * OBSERVE DELETED STACK NAME
         */
        cardStackViewModel.getDeletedStack().observe(getViewLifecycleOwner(), deletedStack -> {
            this.adapter.updateData(directory);
            enforceMinimumSelection();
            if (deletedStack.equals(this.cardStackViewModel.getStackName().getValue())) {
                // if a new stack is subsequently created with the same name,
                //  MainActivity needs to be able to observe the change
                this.cardStackViewModel.selectStack(null);
            }
        });
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
                if (holder instanceof StackSelectionRecyclerViewAdapter.ViewHolder) {
                    return ((StackSelectionRecyclerViewAdapter.ViewHolder)holder).getItemDetails();
                }
            }
            return null;
        }
    }
}