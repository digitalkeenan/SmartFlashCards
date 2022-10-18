package com.example.smartflashcards.recyclers.recycler;

import static java.util.Objects.nonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartflashcards.R;
import com.example.smartflashcards.databinding.FragmentStackViewListBinding;

public class RecyclerFragment extends Fragment {

    public RecyclerView recyclerView;
    private LinearLayoutManager recyclerManager;
    private RecyclerViewAdapter adapter;
    private SelectionTracker tracker;

    private Boolean keysTrackWithItems;

    private Object iterationObject;

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecyclerFragment() {
        setKeysTrackWithItems(false);
    }

    // Set to true if overrides are used to create keys that stay w/ items regardless of position
    public void setKeysTrackWithItems(Boolean keysTrackWithItems) {
        this.keysTrackWithItems = keysTrackWithItems;
    }

    public void setAdapter(RecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentStackViewListBinding binding = FragmentStackViewListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setAdapter(view);
        return view;
    }

    public void setAdapter(View view) {
        View recyclerLayout = view.findViewById(R.id.list);

        if (!nonNull(adapter)) {
            setAdapter(new RecyclerViewAdapter());
        }

        // Set the adapter
        if (recyclerLayout instanceof RecyclerView) {
            Context context = recyclerLayout.getContext();
            this.recyclerManager = new LinearLayoutManager(context);
            this.recyclerView = (RecyclerView) recyclerLayout;
            this.recyclerView.setLayoutManager(this.recyclerManager);
            this.recyclerView.setAdapter(this.adapter);

            this.tracker = new SelectionTracker.Builder<>(
                    "item_selector",
                    recyclerView,
                    new MyKeyProvider(1),
                    new MyDetailsLookup(recyclerView),
                    StorageStrategy.createLongStorage()
                ).withSelectionPredicate(SelectionPredicates.<Long>createSelectSingleAnything())
                    .build();
            this.adapter.injectTracker(this.tracker);

            SelectionTracker.SelectionObserver observer = new SelectionTracker.SelectionObserver() {
                @Override
                public void onSelectionChanged() {
                super.onSelectionChanged();
                callFixSelection();
                }
            };
            this.tracker.addObserver(observer);

            callFixSelection(); // for initial selection
        }
    }

    // better to override these two methods to use keys that stay with items regardless of position
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
                if (holder instanceof RecyclerViewAdapter.ViewHolder) {
                    return ((RecyclerViewAdapter.ViewHolder)holder).getItemDetails();
                }
            }
            return null;
        }
    }

    private void selectItem(Integer position) {
        if (nonNull(position)) {
            Object key = getItemKey(position);
            if (nonNull(key)) {
                this.tracker.select(key);
            }
        }
    }

    private void callFixSelection() {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                //TODO: need a better solution to waiting for selection update to be done...
                // see the github example this has been following and try on-activated again
                call2FixSelection();
            }
        });
    }
    private void call2FixSelection() {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                Integer selectionPosition = getSelectionPosition();
                fixSelection(selectionPosition);
            }
        });
    }
    // this is separate from the call above so that it can be overridden
    public Integer fixSelection(Integer newSelectionPosition) {
        Integer mySelectionPosition = newSelectionPosition;
        if (this.adapter.getItemCount() > 0) {
            //Check for de-selection - if so, select top item
            //This prevents the user from ever having to long-click
            if (nonNull(mySelectionPosition)) {
                int lastItemPosition = this.adapter.getItemCount() - 1;
                if (mySelectionPosition > lastItemPosition) {
                    // if selected item was last, but was deleted, select previous (new last)
                    mySelectionPosition = lastItemPosition;
                    selectItem(mySelectionPosition);
                }
                    // else, selection is good
            } else {
                mySelectionPosition = Math.max(0, this.recyclerManager.findFirstCompletelyVisibleItemPosition());
                selectItem(mySelectionPosition);
            }

            // if selection is not in view, scroll to it (and one further)
            int childCount = this.recyclerManager.getChildCount();
            if (childCount > 1) {
                // trying to do this with childCount = 0 results in negative numbers and out-of-bounds error
                int firstChild = this.recyclerManager.findFirstVisibleItemPosition();
                int lastChild = firstChild + childCount - 1;
                int showBefore = Math.max(0, (mySelectionPosition - 1));
                int showAfter = Math.min((this.adapter.getItemCount() - 1), (mySelectionPosition + 1));
                if (showAfter > lastChild) {
                    recyclerManager.scrollToPosition(showAfter);
                } else if (showBefore < firstChild) {
                    recyclerManager.scrollToPosition(showBefore);
                }
            }
        }
        return mySelectionPosition;
    }

    public int getVisibleItemCount() {
        return this.recyclerManager.getChildCount();
    }

    public Object getSelection() {
        iterationObject = null;
        this.tracker.getSelection().forEach(listItem -> {
            iterationObject = listItem;
        });
        return iterationObject;
    }

    public Integer getSelectionPosition() {
        Object item = getSelection();
        return getItemPosition(item);
    }

    public String getSelectionString() {
        Integer position = getSelectionPosition();
        if (nonNull(position)) {
            return getItemString(position);
        }
        return null;
    }

    public String getItemString(int position) {
        return this.adapter.getString(position);
    }


    /**
     * CHANGE NOTIFICATIONS
     */
    // this stuff will run on the UI (user interface) thread
    // this is necessary to avoid crashing into the layout manager
    // else, find some other way to wait for (!recyclerView.isComputingLayout && recyclerView.scrollState == SCROLL_STATE_IDLE)

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged() {
        selectItem(0); // must clear selection first (not in UI)
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void notifySelectionChanged(int position) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                selectItem(position);
            }
        });
    }

    public void notifyItemInserted(int position) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(position);
                selectItem(position);
            }
        });
    }

    public void notifyItemRangeInserted(int position, int count) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemRangeInserted(position, count);
                // no selection change; if needed, call notifySelectionChanged
            }
        });
    }

    public void notifyItemRemoved(int position, boolean fixSelection) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (keysTrackWithItems) {
                    adapter.notifyItemRemoved(position);
                } else {
                    adapter.notifyDataSetChanged();
                }
                // Select the item that follows the removed item; else select the last item
                // (this would happen automatically for default keys=positions)
                int itemCount = adapter.getItemCount();
                if (fixSelection && (itemCount > 0)) {
                    if (position < itemCount) {
                        selectItem(position);
                    } else {
                        selectItem(itemCount - 1);
                    }
                }
            }
        });
    }

    public void notifyItemChanged(int position) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
                selectItem(position);
            }
        });
    }

    public void notifyItemMoved(int oldPosition, int newPosition) {
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (keysTrackWithItems) {
                    adapter.notifyItemMoved(oldPosition, newPosition);
                } else {
                    adapter.notifyDataSetChanged();
                }

                selectItem(newPosition);
            }
        });
    }
}