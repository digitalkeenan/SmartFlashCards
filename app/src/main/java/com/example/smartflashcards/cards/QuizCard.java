package com.example.smartflashcards.cards;

import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.IOException;
import java.util.Hashtable;

public class QuizCard extends FlashCard {

    //state variables
    private Hashtable correctAnswerCount;
    private int numberWrongResponses; // use this instead of Boolean for such purposes as only crediting a jeopardy response if it is correct the first time
    private int lastPlacement;
    private Boolean jeopardyCard;

    //constants
    protected static final int jeopardyMultiplier = 3; // an answer is in jeopardy if given less than the last answer's count divided by this number

    public QuizCard(String question, int position, Boolean jeopardyCard) {
        // moveDistance to be initialized considering how far it should move if first time the answer given is wrong
        super(question);
        this.cardText = question;
        this.correctAnswerCount = new Hashtable();
        this.numberWrongResponses = 0;
        this.lastPlacement = position;
        this.jeopardyCard = jeopardyCard;
    }

    public QuizCard(MyFileInputStream inputStream) {
        super(inputStream); //reads the question string
        this.correctAnswerCount = new Hashtable();
        try {
            this.numberWrongResponses = inputStream.read();
            this.lastPlacement = inputStream.read();
            this.jeopardyCard = inputStream.readBoolean();
            int numAnswers = inputStream.read();
            for (int answer = 0; answer < numAnswers; answer++) {
                int key = inputStream.read();
                int val = inputStream.read();
                correctAnswerCount.put(key, val);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(MyFileOutputStream outputStream) {
        super.writeFile(outputStream);
        try {
            outputStream.write(this.numberWrongResponses);
            outputStream.write(this.lastPlacement);
            outputStream.writeBoolean(this.jeopardyCard);
            outputStream.writeHashInt(this.correctAnswerCount);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearStatistics() {
        this.correctAnswerCount = new Hashtable();
        this.lastPlacement = 10;// TODO: change this to use variable (statistically driven?)
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

    public void setPosition(Integer position) {
        this.lastPlacement = position;
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

    public Boolean isJeopardy() {
        return this.jeopardyCard;
    }

    public Hashtable getCorrectAnswerCount() {
        return this.correctAnswerCount;
    }
}
