package com.example.smartflashcards.cardTrees;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cards.FlashCard;
import com.example.smartflashcards.keenanClasses.MyCollator;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;
import com.example.smartflashcards.keenanClasses.TreeNode;

import java.io.IOException;
import java.util.Locale;

public class AlphabeticalCardTree extends BinaryCardTree {

    private MyCollator collator;

    public AlphabeticalCardTree(Locale locale) {
        super();
        this.collator = new MyCollator(locale);
    }

    public void setLocale(Locale locale) {
        this.collator = new MyCollator(locale);
    }

    public void writeFile(MyFileOutputStream outputStream) {
        try {
            outputStream.write(getNextID());
            outputStream.write(size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        getHeadNode(); //set current node to the head node
        for (int cardIndex=0; cardIndex < size(); cardIndex++) {
            try {
                outputStream.write(getCurrentNode().getID());
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCard().writeFile(outputStream);
            nextTopDown();
        }
    }

    private CardTreeNode moveToPrev(Boolean optimize) {
        //optimize below BEFORE going down (otherwise search breaks)
        TreeNode prevNode = getCurrentNode().getPrevNode();
        if (optimize && nonNull(prevNode)) {
            optimizeTree(prevNode);
        }
        return moveToPrev();
    }

    private CardTreeNode moveToNext(Boolean optimize) {
        //optimize below BEFORE going down (otherwise search breaks)
        TreeNode nextNode = getCurrentNode().getNextNode();
        if (optimize && nonNull(nextNode)) {
            optimizeTree(nextNode);
        }
        return moveToNext();
    }

    public FlashCard findCard (String cardText, Boolean optimize) {
        TreeNode currentNode = getHeadNode(); // set this and super current node to head node
        if (nonNull(currentNode)) {
            if (optimize) {
                optimizeTree(currentNode); //TODO: test if opti's in this routine cause any performance issues
                getHeadNode();
            }
        } else {
            return null;
        }

        while (!cardText.equals(getCard().getCardText())) {
            if (collator.lessThan(cardText, getCard().getCardText())) {
                if (!nonNull(moveToPrev(optimize))) { // moves current node to previous
                    //if current node is now null, search is complete and unsuccessful
                    return null;
                }
            } else {
                if (!nonNull(moveToNext(optimize))) { // moves current node to next
                    //if current node is now null, search is complete and unsuccessful
                    return null;
                }
            }
        }
        return getCard();
    }

    public Integer findCardStartsWith (String pattern, Boolean optimize) {
        TreeNode currentNode = getHeadNode(); // set this and super current node to head node
        if (nonNull(currentNode) && nonNull(pattern)) {
            if (optimize) {
                super.optimizeTree(currentNode);
                super.getHeadNode();
            }
        } else {
            return null;
        }
        //find a node with text that starts with the pattern
        while (!getCard().getCardText().startsWith(pattern)) {
            if (collator.lessThan(pattern, getCard().getCardText())) {
                if (!nonNull(moveToPrev(optimize))) { // moves current node to previous
                    //if current node is now null, search is complete and unsuccessful
                    return null;
                }
            } else {
                if (!nonNull(moveToNext(optimize))) { // moves current node to next
                    //if current node is now null, search is complete and unsuccessful
                    return null;
                }
            }
        }
        //check for sequentially earlier nodes that also start with the pattern
        Integer position = getPosition();
        while (nonNull(previousSequential()) && getCard().getCardText().startsWith(pattern)) {
            position = getPosition();
        }
        return position;
    }

    public FlashCard addNode (FlashCard card, Integer idNumber, Boolean optimize) {
        //returns null when successful
        //returns a card if one is found to already exist

        this.findCard(card.getCardText(), optimize);

        if (nonNull(getCurrentNode())) {
            //found an existing card
            return getCard();
        } else {
            super.addNode(card, idNumber); //adds below super.parentNode or as first/head node
        }
        return null;
    }

    public FlashCard rename (String oldText, String newText, boolean keepID) {
        FlashCard flashCard = findCard(oldText, true);
        int oldID = getCurrentID();
        deleteNode();
        flashCard.modifyCardText(newText);
        findCard(newText, true); // setup location to place updated card
        if (keepID) {
            addNode(flashCard, oldID, true);
        } else {
            addNode(flashCard, null, true);
        }
        return flashCard;
    }
}
