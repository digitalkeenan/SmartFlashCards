package com.example.smartflashcards.recyclers.cardEditor;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.recyclers.recycler.RecyclerViewAdapter;

public class CardEditorRecyclerViewAdapter extends RecyclerViewAdapter {

    private CardStackViewModel cardStackViewModel;

    public CardEditorRecyclerViewAdapter(CardStackViewModel cardStackViewModel) {
        super();
        this.cardStackViewModel = cardStackViewModel;
    }

    @Override
    public int getItemCount() {
        return this.cardStackViewModel.getSelectionCard().reviewStrings().size();
    }

    @Override
    public String getString(int position) {
        return this.cardStackViewModel.getSelectionCard().reviewStrings().get(position);
    }
}