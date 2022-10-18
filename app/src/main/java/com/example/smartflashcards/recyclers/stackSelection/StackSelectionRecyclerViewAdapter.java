package com.example.smartflashcards.recyclers.stackSelection;

import static java.util.Objects.nonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartflashcards.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackSelectionRecyclerViewAdapter extends RecyclerView.Adapter<StackSelectionRecyclerViewAdapter.ViewHolder> {

    private List<String> mValues;
    private SelectionTracker<Long> selectionTracker;

    public StackSelectionRecyclerViewAdapter() {
        setHasStableIds(true);
    }

    //TODO: change this to be an override of notifyDataSetChanged()
    public void updateData(File directory) {
        mValues = new ArrayList(Arrays.asList(directory.list()));
        mValues.sort(null);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_one_column, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = this.mValues.get(position);
        holder.bind(item, selectionTracker.isSelected((long) position));
    }

    @Override
    public int getItemCount() {
        //if null, return 0
        // this is needed for initial details lookup BEFORE view created (which calls updateData)
        if (nonNull(mValues)) {
            return mValues.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    public String getString(int position) {
        return mValues.get(position);
    }

    //androidx.recyclerview.selection documentation says "inject the SelectionTracker instance into your RecyclerView.Adapter"?
    public void injectTracker(SelectionTracker<Long> tracker)
    {
        this.selectionTracker = tracker;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContentView = itemView.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void bind(String item, Boolean activate) {
            this.mContentView.setText(item);
            super.itemView.setActivated(activate);
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getAbsoluteAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    return (long) getAbsoluteAdapterPosition();
                }
            };
        }
    }
}