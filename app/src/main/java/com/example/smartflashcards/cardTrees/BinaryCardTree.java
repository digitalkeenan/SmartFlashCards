package com.example.smartflashcards.cardTrees;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cards.FlashCard;
import com.example.smartflashcards.keenanClasses.BinaryTree;

import java.util.ArrayList;

public class BinaryCardTree extends BinaryTree {

    public BinaryCardTree() {
        super();
    }

    public CardTreeNode getCurrentNode() {
        return (CardTreeNode) super.getCurrentNode();
    }

    public CardTreeNode moveToFirst() {
        return (CardTreeNode) super.moveToFirst();
    }

    public CardTreeNode moveToPrev() {
        return (CardTreeNode) super.moveToPrev();
    }

    public CardTreeNode moveToNext() {
        return (CardTreeNode) super.moveToNext();
    }

    public FlashCard getCard () {
        CardTreeNode currentNode = getCurrentNode();
        if (nonNull(currentNode)) {
            return currentNode.getCard();
        }
        return null;
    }

    public FlashCard getCard (int position) {
        moveToPosition(position);
        return getCard();
    }

    // ID's are needed to prevent multiple valid quiz cards existing for the same question card
    // - this would happen by a question being deleted and then recreated
    // ID's can't be used for recycleView keys because must be able to look up position given key
    public CardTreeNode addNode (FlashCard card, Integer idNumber) {
        int myID;
        if (nonNull(idNumber)) {
            myID = idNumber;
        } else {
            myID = getAndIncrementNextID();
        }
        CardTreeNode newNode = new CardTreeNode(myID, card);
        addNode(newNode);
        return newNode;
    }

    public CardTreeNode addToEnd (FlashCard card, int idNumber) {
        CardTreeNode newNode = new CardTreeNode(idNumber, card);
        addToEnd(newNode);
        return newNode;
    }

    public FlashCard findNextContains(String pattern) {
        if (nonNull(getCurrentNode()) && nonNull(pattern) && !pattern.equals("")) {
            //find a node that contains the pattern
            while (!getCard().getCardText().toLowerCase().contains(pattern.toLowerCase())) {
                if (!nonNull(nextSequential())) { // moves current node to sequentially next node
                    //if current node is now null, search is complete and unsuccessful
                    return null;
                }
            }
            return getCard();
        }
        setCurrentNode(null);
        return null;
    }

    public ArrayList<CardTreeNode> getNodes() {
        ArrayList<CardTreeNode> nodes = new ArrayList<>();
        if (nonNull(moveToFirst())) {
            nodes.add(getCurrentNode());
            while (nonNull(nextSequential())) {
                nodes.add(getCurrentNode());
            }
        }
        return nodes;
    }
}
