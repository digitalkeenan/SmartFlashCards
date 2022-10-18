package com.example.smartflashcards.recyclers.quizCardReview;

import static java.util.Objects.nonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartflashcards.R;

import java.util.ArrayList;

public class QuizCardViewAdapter extends RecyclerView.Adapter<QuizCardViewAdapter.ViewHolder> {

    private ArrayList<String> answers = new ArrayList<>();
    private ArrayList<Integer> counts = new ArrayList<>();

    public void addData(String answer, int count) {
        this.answers.add(answer);
        this.counts.add(count);
    }

    public void clearData() {
        this.answers = new ArrayList<>();
        this.counts = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_two_columns, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String column1 = answers.get(position);
        Integer column2 = counts.get(position);
        if (nonNull(column1) && nonNull(column2)) {
            holder.bind(column1, column2.toString());
        }
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    public Object getItemKey(int position) {
        return (long)position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView column1View;
        public final TextView column2View;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            column1View = itemView.findViewById(R.id.column1);
            column2View = itemView.findViewById(R.id.column2);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + column1View.getText() + "'";
        }

        public void bind(String column1, String column2) {
            this.column1View.setText(column1);
            this.column2View.setText(column2);
        }

        public ItemDetailsLookup.ItemDetails<Object> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Object>() {
                @Override
                public int getPosition() {
                    return getAbsoluteAdapterPosition();
                }

                //TODO: try to remove this selection stuff
                @Nullable
                @Override
                public Object getSelectionKey() {
                    return getItemKey(getAbsoluteAdapterPosition());
                }
            };
        }
    }
}