package com.example.smartflashcards.recyclers.recycler;

import static java.util.Objects.nonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartflashcards.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private SelectionTracker<Object> selectionTracker;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_one_column, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = getString(position);
        if (nonNull(item)) {
            holder.bind(item, selectionTracker.isSelected(getItemKey(position)));
        }
    }

    @Override
    public int getItemCount() {
        //Inheriting adapter must override this - only dummy data provided here
        return 10;
    }

    public Object getItemKey(int position) {
        return (long)position;
    }

    public String getString(int position) {
        //Inheriting adapter must override this - only dummy data provided here
        return "Item #" + position;
    }

    //androidx.recyclerview.selection documentation says "inject the SelectionTracker instance into your RecyclerView.Adapter"?
    public void injectTracker(SelectionTracker<Object> tracker)
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

        public ItemDetailsLookup.ItemDetails<Object> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Object>() {
                @Override
                public int getPosition() {
                    return getAbsoluteAdapterPosition();
                }

                @Nullable
                @Override
                public Object getSelectionKey() {
                    return getItemKey(getAbsoluteAdapterPosition());
                }
            };
        }
    }
}