package com.example.smartflashcards.cardTrees;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cards.FlashCard;
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
            QuizCard quizCard = new QuizCard(node.getCard().getCardText(), false);
            addNode(node.getID(), quizCard, -1, true);
            questionNodes.remove(nodeNumber);
        }
    }

    @Override
    public CardTreeNode addNode(FlashCard card, Integer idNumber) {
        int lastPlacement = ((QuizCard)card).getLastPlacement();
        if (lastPlacement < -1) {
            this.incrementGoldStars();
        } else if (lastPlacement < 0) {
            this.incrementSilverStars();
        }
        return super.addNode(card, idNumber);
    }

    @Override
    public CardTreeNode addToEnd(FlashCard card, int idNumber) {
        int lastPlacement = ((QuizCard)card).getLastPlacement();
        if (lastPlacement < -1) {
            this.incrementGoldStars();
        } else if (lastPlacement < 0) {
            this.incrementSilverStars();
        }
        return super.addToEnd(card, idNumber);
    }

    @Override
    public void deleteNode() {
        int lastPlacement = ((QuizCard)this.getCurrentNode().getCard()).getLastPlacement();
        if (lastPlacement < -1) {
            this.decrementGoldStars();
        } else if (lastPlacement < 0) {
            this.decrementSilverStars();
        }
        super.deleteNode();
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

    public int addNode(int idNumber, QuizCard card, int position, boolean clearStats) {
        // this should be used for any artificial (not advanced by quiz) placement / movement
        // if clearStats, placement value is set to starting value
        // else, placement value is left unchanged
        int last_position = size(); // new last position after adding this one

        if (clearStats) {
            card.setStatusPosition(startPosition);
            card.setLastPlacement(0); // actual position is only meaningful when placed by quiz
        }

        // if stack is empty, add as first/head node
        if (size() == 0) {
            addNode(card, idNumber);
            card.setLastPlacement(0);
            return 0;
        }

        // if placement is set to -1 or beyond last position, place it last
        if ((position < 0) || (position > last_position)) {
            moveToLast();
            addToEnd(card, idNumber); //adds to end with a tree balancing routine
            return last_position;
        }

        // if placement = 0, place at previous to first
        if (position == 0) {
            moveToFirst();
            moveToPrev();
            addNode(card, idNumber); //adds below super.parentNode
            return 0;
        }

        // if not placed above, place in numerical position given
        getCard(position - 1);
        moveToNext();
        //if this position is empty, add here, otherwise, add at furthest prev position
        while (nonNull(getCurrentNode())) {
            moveToPrev();
        }
        addNode(card, idNumber); //adds below super.parentNode
        return position;
    }

    public void reInsertQuizMeNode(int idNumber, QuizCard card, boolean correct, int adjustor, int limit) {
        // Encodes lastPlacement = -1 for silver star and -2 for gold star
        // if silver and statusPosition is less than total number of cards, that is the actual placement
        int newStatus;
        int last_position = size(); // new last position after adding this one
        int lastPlacement = card.getLastPlacement();
        int lastStatus = card.getStatusPosition();

        // if NO OTHER CARDS, just put it back and fix placement = 0
        if (size() == 0) {
            addNode(card, idNumber); //adds as first/head node
            card.setLastPlacement(0);
            return;
        }

        if (correct) {
            // if ALREADY GOLD, put back at end and only increase status incrementally
            if (lastPlacement < -1) {
                card.setStatusPosition(lastStatus + size());
                moveToLast();
                addToEnd(card, idNumber); //adds to end with a tree balancing routine
                return;
            }

            if (lastStatus == 0) {
                newStatus = 2;
            } else {
                newStatus = lastStatus * adjustor;
            }

            // TODO: consider placing cards ahead of similar cards with higher status numbers
            //       this should be fairly easy for star cards
            //       For non-star cards getting limited by star depth, this is harder (maybe only do this for star cards)
            //       Even for star cards, perhaps this is a bad idea, because the user should be tested with the current card pushed far (4x isn't even possible)be

            if (lastPlacement < 0) { // ALREADY SILVER
                card.setStatusPosition(newStatus);
                if (newStatus > last_position) {
                    card.setLastPlacement(-2); // new GOLD STAR
                    moveToLast();
                    addToEnd(card, idNumber); //adds to end with a tree balancing routine
                    return;
                }
                // if newStatus <= last_position, this is a silver star not yet ready for gold
                getCard(newStatus - 1);
            } else if (newStatus > last_position) {
                card.setLastPlacement(-1); // new SILVER STAR
                // for a new silver star, only move it deeper relative to its actual last placement
                if (lastPlacement == 0) {
                    newStatus = 2;
                } else {
                    newStatus = lastPlacement * adjustor;
                }
                card.setStatusPosition(newStatus);
                if (newStatus > last_position) {
                    moveToLast();
                    addToEnd(card, idNumber); //adds to end with a tree balancing routine
                    return;
                }
                // if newStatus <= last_position, place at actual newStatus location
                getCard(newStatus - 1);
            } else {
                // NON-STAR placement might be limited
                card.setStatusPosition(newStatus);
                if ((limit > 0) && (newStatus > limit)) {
                    getCard(limit - 1);
                    card.setLastPlacement(limit);
                } else {
                    getCard(newStatus - 1);
                    card.setLastPlacement(newStatus);
                }
            }
        } else {
            // WRONG ANSWER
            if (lastPlacement < 0) {
                newStatus = Math.min(limit, size() - 1) / adjustor;
            } else if (lastPlacement == 0) {
                // if lastStatus isn't also 0, lastPlacement is not yet real
                newStatus = Math.min(limit, lastStatus) / adjustor;
            } else {
                newStatus = Math.min(limit, lastPlacement) / adjustor;
            }
            card.setLastPlacement(newStatus);
            card.setStatusPosition(newStatus);
            if (newStatus == 0) {
                moveToFirst();
                moveToPrev();
                addNode(card, idNumber); //adds below super.parentNode
                return;
            }
            getCard(newStatus - 1);
        }

        // place after card selected above
        moveToNext();
        //if this position is empty, add here, otherwise, add at furthest prev position
        while (nonNull(getCurrentNode())) {
            moveToPrev();
        }
        addNode(card, idNumber); //adds below super.parentNode
        return;
     }

    public ArrayList<Integer> getStats() {
        ArrayList<Integer> stats = new ArrayList<>(6);
        int gradeAscore;
        int gradeBscore;
        int gradeCscore;
        int lastStatus;
        for (int index = 0; index < 6; index++) {
            stats.add(0);
        }

        //int size = size();
        //if (size > 512) {
            gradeAscore = 512;
            gradeBscore = 128;
            gradeCscore = 32;
            // TODO: user settable grade threshold(s) or some sort of statistical idea?
            //       below is for using lastPlacement instead of lastStatus
        /*} else {
            gradeAscore = size - 1;
            gradeBscore = gradeAscore / 4;
            gradeCscore = gradeBscore / 4;
        }*/
        moveToFirst();
        QuizCard quizCard = (QuizCard) getCard();
        while (nonNull(quizCard)) {
            lastStatus = quizCard.getStatusPosition();
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
                // A with star (silver star drops status back down, but still grades as A)
                stats.set(5, stats.get(5) + 1);
            } else if (lastStatus >= gradeAscore) {
                // A
                stats.set(5, stats.get(5) + 1);
            } else if (lastStatus >= gradeBscore) {
                // B
                stats.set(4, stats.get(4) + 1);
            } else if (lastStatus >= gradeCscore) {
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
