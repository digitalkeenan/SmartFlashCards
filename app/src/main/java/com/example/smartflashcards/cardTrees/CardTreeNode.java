package com.example.smartflashcards.cardTrees;

import com.example.smartflashcards.cards.FlashCard;
import com.example.smartflashcards.keenanClasses.TreeNode;

public class CardTreeNode extends TreeNode {

    //state variables
    private FlashCard flashCard;

    public CardTreeNode (int idNumber, FlashCard flashCard) {
        super(idNumber);
        this.flashCard = flashCard;
    }

    public FlashCard getCard() {
        return this.flashCard;
    }

}
