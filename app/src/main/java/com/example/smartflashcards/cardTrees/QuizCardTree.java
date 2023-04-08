package com.example.smartflashcards.cardTrees;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class QuizCardTree extends BinaryCardTree {

    /* for any new word added, pretend it started in this position,
       next placing it further or closer based on correct or incorrect response
       TODO: add adjustment of this value based on user success with second attempt */
    protected int startPosition = 10;

    private int goldStars = 0;
    private int silverStars = 0;

    private Boolean iterationBoolean;

    public QuizCardTree() {
        super();
        clearStars();
    }

    public QuizCardTree(String version, MyFileInputStream inputStream) {
        this();
        //TODO: better handle and/or prevent the null case and change above to "(@NonNull MyFileInputStream inputStream)"

        int numCards = 0;
        QuizCard card;

        if (nonNull(inputStream)) {
            if (version.equals("0.02")) {
                try {
                    numCards = inputStream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                numCards = inputStream.readInt();
            }

            if (numCards > 0) {
                int idNumber = 0;
                for (int cardIndex = 0; cardIndex < numCards; cardIndex++) {
                    if (version.equals("0.02")) {
                        try {
                            idNumber = inputStream.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        idNumber = inputStream.readInt();
                    }
                    card = new QuizCard(version, inputStream);
                    if (card.getLastPlacement() < -1) {
                        this.goldStars += 1;
                    } else if (card.getLastPlacement() < 0) {
                        this.silverStars += 1;
                    }
                    if (cardIndex == 0) {
                        addNode(card, idNumber);
                    } else {
                        addToEnd(card, idNumber); //requires a non-empty tree with currentNode set to last node
                    }
                }
            }
        }
    }

    // Build fresh tree with all questions in random order
    public QuizCardTree(ArrayList<CardTreeNode> questionNodes) {
        this();

        Random random = new Random();
        for (int nodesRemaining = questionNodes.size(); nodesRemaining > 0; nodesRemaining--) {
            int nodeNumber = random.nextInt(nodesRemaining); // random number from 0 to nodesRemaining - 1
            CardTreeNode node = questionNodes.get(nodeNumber);
            QuizCard quizCard = new QuizCard(node.getCard().getCardText(), -1, false);
            addNode(node.getID(), quizCard, true, 0);
            questionNodes.remove(nodeNumber);
        }
    }

    public void writeFile (MyFileOutputStream outputStream) {
        outputStream.writeInt(size());

        super.moveToFirst(); //set current node to the first sequential node
        for (int cardIndex=0; cardIndex < size(); cardIndex++) {
            outputStream.writeInt(getCurrentNode().getID());
            getCard().writeFile(outputStream);
            nextSequential();
        }
    }

    public void clearStars () {
        this.goldStars = 0;
        this.silverStars = 0;
    }

    public int getGoldStars () {
        return this.goldStars;
    }

    public int getSilverStars () {
        return this.silverStars;
    }

    public int incrementGoldStars () {
        return ++this.goldStars;
    }

    public int incrementSilverStars () {
        return ++this.silverStars;
    }

    public int decrementGoldStars () {
        if (this.goldStars > 0) {
            return --this.goldStars;
        }
        return 0;
    }

    public int decrementSilverStars () {
        if (this.silverStars > 0) {
            return --this.silverStars;
        }
        return 0;
    }

    public int addNode (int idNumber, QuizCard card, Boolean newCard, int limit) {
        // newCard means placement value is set to starting value - not actual position
        // - this should be used for any artificial (not advanced by quiz success) placement / movement
        // - if not newCard and position after last, place as silver star (-1) or gold star(-2)
        int position;
        int last_position = size(); // new last position after adding this one
        boolean limited_placement = false;

        if (size() == 0) {
            addNode(card, idNumber); //adds as first/head node
            position = startPosition;
        } else {
            // if placement is set to -1 or beyond last position, place it last
            // it may also be placed in last position if placement equals last,
            // but there it might be randomized elsewhere
            if (card.getLastPlacement() < 0) {
                moveToLast();
                addToEnd(card, idNumber); //adds to end with a tree balancing routine
                position = -2; // gold star
            } else if (card.getLastPlacement() > last_position) {
                moveToLast();
                addToEnd(card, idNumber); //adds to end with a tree balancing routine
                position = -1; // silver star
            } else if (card.getLastPlacement() == 0) {
                moveToFirst();
                moveToPrev();
                addNode(card, idNumber); //adds below super.parentNode
                position = 0;
            } else {
                position = card.getLastPlacement();
                if ((limit > 0) && (position > limit)) {
                    getCard(limit - 1);
                    limited_placement = true;
                } else {
                    getCard(position - 1);
                }
                moveToNext();
                //if this position is empty, add here, otherwise, add at furthest prev position
                while (nonNull(getCurrentNode())) {
                    moveToPrev();
                }
                addNode(card, idNumber); //adds below super.parentNode
            }
        }
        if (newCard) {
            card.setPosition(startPosition);
        } else {
            card.setPosition(position);
        }

        if (limited_placement) {
            return limit;
        }
        return position;
    }

    public ArrayList<Integer> getStats() {
        ArrayList<Integer> stats = new ArrayList<>(6);
        int gradeAscore;
        int gradeBscore;
        int gradeCscore;
        for (int index = 0; index < 6; index++) {
            stats.add(0);
        }

        int size = size();
        if (size > 512) {
            gradeAscore = 512;
            gradeBscore = 128;
            gradeCscore = 32;
        } else {
            gradeAscore = size - 1;
            gradeBscore = gradeAscore / 4;
            gradeCscore = gradeBscore / 4;
        }
        moveToFirst();
        QuizCard quizCard = (QuizCard) getCard();
        while (nonNull(quizCard)) {
            Hashtable answerCounts = quizCard.getCorrectAnswerCount();
            iterationBoolean = false;
            answerCounts.forEach((key, count) -> {
                if ((int)count > 0) {
                    iterationBoolean = true;
                }
            });
            if (quizCard.getNumberWrongResponses() > 0) {
                // F (wrong)
                stats.set(1, stats.get(1) + 1);
            } else if (!iterationBoolean) {
                // I (not tried)
                stats.set(0, stats.get(0) + 1);
            } else if (quizCard.getLastPlacement() < 0) {
                // A with star
                stats.set(5, stats.get(5) + 1);
            } else if (quizCard.getLastPlacement() >= gradeAscore) {
                // A
                stats.set(5, stats.get(5) + 1);
            } else if (quizCard.getLastPlacement() >= gradeBscore) {
                // B
                stats.set(4, stats.get(4) + 1);
            } else if (quizCard.getLastPlacement() >= gradeCscore) {
                // C
                stats.set(3, stats.get(3) + 1);
            } else {
                // D
                stats.set(2, stats.get(2) + 1);
            }
            nextSequential();
            quizCard = (QuizCard) getCard();
        }
        return stats;
    }
}
