package com.example.smartflashcards.cards;

import com.example.smartflashcards.keenanClasses.MyAutoCloseInputStream;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.util.ArrayList;

public class FlashCard {

    protected String cardText;

    public FlashCard (String string) {
        this.cardText = string;
    }

    public FlashCard (MyFileInputStream inputStream) {
        this.cardText = inputStream.readString();
    }
    public FlashCard (MyAutoCloseInputStream inputStream) {
        this.cardText = inputStream.readString();
    }

    public void writeFile (MyFileOutputStream outputStream) {
        outputStream.writeString(this.cardText);
    }

    public String getCardText() {
        return this.cardText;
    }

    public void modifyCardText(String string) {
        this.cardText = string;
    }

    // Intended to be overridden
    // TODO: consider changing these to alphabetized lists with keys for recycler
    //  this would improve animations of adding, removing, changing a string
    public ArrayList<String> reviewStrings() {
        return null;
    }
}
