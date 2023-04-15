package com.example.smartflashcards.cards;

import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.IOException;
import java.util.Hashtable;

public class QuizCard extends FlashCard {

    //state variables
    private Hashtable correctAnswerCount;
    private int numberWrongResponses; // use this instead of Boolean for such purposes as only crediting a jeopardy response if it is correct the first time
    private int lastPlacement; // actual last placement or -1 for silver and -2 for gold
    private int statusPosition; // theoretical position if actual placement wasn't adjusted
                                // for silver star cards with limited placement, this is actual
    private Boolean jeopardyCard;

    //constants
    protected static final int jeopardyMultiplier = 3; // an answer is in jeopardy if given less than the last answer's count divided by this number

    public QuizCard(String question, Boolean jeopardyCard) {
        // moveDistance to be initialized considering how far it should move if first time the answer given is wrong
        super(question);
        this.cardText = question;
        this.correctAnswerCount = new Hashtable();
        this.numberWrongResponses = 0;
        this.lastPlacement = 0; // placement to be set when node is added to tree
        this.statusPosition = 0;
        this.jeopardyCard = jeopardyCard;
    }

    public QuizCard(String version, MyFileInputStream inputStream) {
        super(inputStream); //reads the question string
        int numAnswers = 0;
        if (version.equals("0.02")) {
            this.correctAnswerCount = new Hashtable();
            try {
                this.numberWrongResponses = inputStream.read();
                this.lastPlacement = inputStream.read();
                if (this.lastPlacement < 0) {
                    this.statusPosition = 10; // arbitrary value because quiz size is unknown here
                } else {
                    this.statusPosition = this.lastPlacement;
                }
                this.jeopardyCard = inputStream.readBoolean();
                numAnswers = inputStream.read();
                for (int answer = 0; answer < numAnswers; answer++) {
                    int key = inputStream.read();
                    int val = inputStream.read();
                    correctAnswerCount.put(key, val);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (version.equals("0.03")){
            try {
                this.numberWrongResponses = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.lastPlacement = inputStream.readInt();
            if (this.lastPlacement < 0) {
                this.statusPosition = 10; // arbitrary value because quiz size is unknown here
            } else {
                this.statusPosition = this.lastPlacement;
            }
            this.jeopardyCard = inputStream.readBoolean();
            try {
                numAnswers = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.correctAnswerCount = inputStream.readHashInt(numAnswers);
        } else {
            try {
                this.numberWrongResponses = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.lastPlacement = inputStream.readInt();
            this.statusPosition = inputStream.readInt();
            this.jeopardyCard = inputStream.readBoolean();
            try {
                numAnswers = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.correctAnswerCount = inputStream.readHashInt(numAnswers);
        }
    }

    public void writeFile(MyFileOutputStream outputStream) {
        super.writeFile(outputStream);
        try {
            outputStream.write(this.numberWrongResponses);
            outputStream.writeInt(this.lastPlacement);
            outputStream.writeInt(this.statusPosition);
            outputStream.writeBoolean(this.jeopardyCard);
            outputStream.writeHashInt(this.correctAnswerCount);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearStatistics() {
        this.correctAnswerCount = new Hashtable();
        this.lastPlacement = 0;
        this.statusPosition = 10;// TODO: change this to use variable (statistically driven?)
        this.numberWrongResponses = 0;
    }

    public void recordCorrectResponse(Integer key) {
        if (this.correctAnswerCount.containsKey(key)) {
            int newVal = (int) this.correctAnswerCount.get(key) + 1;
            this.correctAnswerCount.replace(key, newVal);
        } else {
            this.correctAnswerCount.put(key, (int)1);
        }
        this.numberWrongResponses = 0;
    }

    public void setLastPlacement(int position) {
        this.lastPlacement = position;
    }

    public void setStatusPosition(int position) {
        this.statusPosition = position;
    }

    public void recordWrongResponse() {
        this.numberWrongResponses++;
    }

    public int getNumberWrongResponses() {
        return this.numberWrongResponses;
    }

    public int getLastPlacement() {
        return this.lastPlacement;
    }

    public int getStatusPosition() {
        return this.statusPosition;
    }

    public Boolean isJeopardy() {
        return this.jeopardyCard;
    }

    public Hashtable getCorrectAnswerCount() {
        return this.correctAnswerCount;
    }
}
