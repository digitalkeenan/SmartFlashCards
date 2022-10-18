package com.example.smartflashcards.cardTrees;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class QuizCardTree extends BinaryCardTree {

    /* for any new word added, pretend it started in this position,
       next placing it further or closer based on correct or incorrect response
       TODO: add adjustment of this value based on user success with second attempt */
    protected int startPosition = 10;

    public QuizCardTree() {
        super();
    }

    public QuizCardTree(String version, MyFileInputStream inputStream) {
        super();
        //TODO: better handle and/or prevent the null case and change above to "(@NonNull MyFileInputStream inputStream)"

        int numCards = 0;
        QuizCard card;

        if (nonNull(inputStream)) {
            try {
                numCards = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (numCards > 0) {
                int idNumber = 0;
                if (!version.equals("0")) {
                    try {
                        idNumber = inputStream.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                card = new QuizCard(inputStream);
                addNode(card, idNumber);

                for (int cardIndex = 1; cardIndex < numCards; cardIndex++) {
                    if (!version.equals("0")) {
                        try {
                            idNumber = inputStream.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    card = new QuizCard(inputStream);
                    addToEnd(card, idNumber); //requires a non-empty tree with currentNode set to last node
                }
            }
        }
    }

    // Build fresh tree with all questions in random order
    public QuizCardTree(ArrayList<CardTreeNode> questionNodes) {
        super();
        Random random = new Random();
        for (int nodesRemaining = questionNodes.size(); nodesRemaining > 0; nodesRemaining--) {
            int nodeNumber = random.nextInt(nodesRemaining); // random number from 0 to nodesRemaining - 1
            CardTreeNode node = questionNodes.get(nodeNumber);
            QuizCard quizCard = new QuizCard(node.getCard().getCardText(), -1, false);
            addNode(node.getID(), quizCard, true);
            questionNodes.remove(nodeNumber);
        }
    }

    public void writeFile (MyFileOutputStream outputStream) {
        try {
            outputStream.write(size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.moveToFirst(); //set current node to the first sequential node
        for (int cardIndex=0; cardIndex < size(); cardIndex++) {
            try {
                outputStream.write(getCurrentNode().getID());
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCard().writeFile(outputStream);
            nextSequential();
        }
    }

    public int addNode (int idNumber, QuizCard card, Boolean newCard) {
        int position;
        int last_position = size() - 1;

        if (size() == 0) {
            addNode(card, idNumber); //adds as first/head node
            position = 0;
        } else {
            // if placement is set to -1 or beyond last position, place it last
            // it may also be placed in last position if placement equals last,
            // but there it might be randomized elsewhere
            if ((card.getLastPlacement() < 0) || (card.getLastPlacement() > last_position)) {
                moveToLast();
                addToEnd(card, idNumber); //adds to end with a tree balancing routine
                position = last_position;
            } else if (card.getLastPlacement() == 0) {
                moveToFirst();
                moveToPrev();
                addNode(card, idNumber); //adds below super.parentNode
                position = 0;
            } else {
                //TODO: add randomization to somewhat earlier (perhaps by multiplying with 0.9 to 1)
                getCard(card.getLastPlacement() - 1);
                moveToNext();
                //if this position is empty, add here, otherwise, add at furthest prev position
                while (nonNull(getCurrentNode())) {
                    moveToPrev();
                }
                addNode(card, idNumber); //adds below super.parentNode
                position = card.getLastPlacement();
            }
        }
        if (newCard) {
            card.setPosition(startPosition);
        } else {
            card.setPosition(position);
        }
        return position;
    }
}
