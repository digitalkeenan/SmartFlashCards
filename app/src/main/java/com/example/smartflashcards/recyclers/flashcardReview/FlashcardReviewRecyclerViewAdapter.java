package com.example.smartflashcards.recyclers.flashcardReview;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.recyclers.recycler.RecyclerViewAdapter;

public class FlashcardReviewRecyclerViewAdapter extends RecyclerViewAdapter {

    private CardStackViewModel cardStackViewModel;

    public FlashcardReviewRecyclerViewAdapter(CardStackViewModel cardStackViewModel) {
        super();
        this.cardStackViewModel = cardStackViewModel;
    }

    @Override
    public int getItemCount() {
        return this.cardStackViewModel.getReviewCard().reviewStrings().size();
    }

    @Override
    public String getString(int position) {
        return this.cardStackViewModel.getReviewCard().reviewStrings().get(position);
    }
}