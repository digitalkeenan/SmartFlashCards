package com.example.smartflashcards.recyclers.stackView;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.recyclers.recycler.RecyclerViewAdapter;

public class StackViewRecyclerViewAdapter extends RecyclerViewAdapter {

    private CardStackViewModel cardStackViewModel;

    public StackViewRecyclerViewAdapter(CardStackViewModel cardStackViewModel) {
        super();
        this.cardStackViewModel = cardStackViewModel;
    }

    @Override
    public Object getItemKey(int position) {
        return this.cardStackViewModel.getViewNode(position);
    }

    @Override
    public int getItemCount() {
        return this.cardStackViewModel.getViewSize();
    }

    @Override
    public String getString(int position) {
        return this.cardStackViewModel.getViewCardString(position);
    }
}