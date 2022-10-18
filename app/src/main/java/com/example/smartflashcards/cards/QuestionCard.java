package com.example.smartflashcards.cards;

import androidx.annotation.NonNull;

import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class QuestionCard extends FlashCard {
    protected Hashtable answers;
    protected int nextKey;

    private int iterationInt;
    private Integer iterationInteger;
    private String iterationString;
    private Boolean iterationBoolean;

    public QuestionCard(String question, String answer) {
        super(question);
        this.answers = new Hashtable();
        this.nextKey = 0;
        this.answers.put(this.nextKey++, answer);
    }

    public QuestionCard(MyFileInputStream inputStream) {
        super(inputStream);
        this.answers = new Hashtable();
        try {
            this.nextKey = inputStream.read();
            int numberAnswers = inputStream.read();
            int key;
            for (int answer = 0; answer < numberAnswers; answer++) {
                key = inputStream.read();
                this.answers.put(key, inputStream.readString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile (@NonNull MyFileOutputStream outputStream) {
        super.writeFile(outputStream);
        try {
            outputStream.write(this.nextKey);
            outputStream.writeHashString(this.answers);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hashtable getAnswers () {
        return answers;
    }

    public void addAnswer (String answer) {
        this.answers.put(this.nextKey++, answer);
    }

    public enum DeleteReturnCode {
        SUCCESS,
        NO_EXTRA_ANSWERS,
        ANSWER_NOT_FOUND,
        ;
    }
    public DeleteReturnCode deleteAnswer (String answer) {
        if (this.answers.containsValue(answer)) {
            if (this.answers.size() == 1) {
                return DeleteReturnCode.NO_EXTRA_ANSWERS; // not allowed to delete the last answer
            }
            // Using iterator with remove function rather than foreach loop
            // can't delete inside foreach loop because that causes ConcurrentModificationException
            Iterator iterator = this.answers.keySet().iterator();
            while (iterator.hasNext()) {
                int key = (int) iterator.next();
                String string = (String) this.answers.get(key);
                if (answer.equals(string)) {
                    iterator.remove();
                    return DeleteReturnCode.SUCCESS;
                }
            }
        }
        return DeleteReturnCode.ANSWER_NOT_FOUND;
    }

    public String getAnswersString () {
        iterationString = "";
        iterationInt = 0;
        getAnswers().forEach((key, answer) -> {
            switch (iterationInt++) {
                case 0:
                    iterationString = (String) answer;
                    break;
                case 1:
                    iterationString = "[" + iterationString + "] & [" + (String) answer + "]";
                    break;
            }
        });
        if (iterationInt > 2) {
            iterationString += " and " + (iterationInt - 2) + " more";
        }

        return iterationString;
    }

    public ArrayList<String> reviewStrings() {
        ArrayList<String> strings = new ArrayList<>();
        getAnswers().forEach((key, answer) -> {
            strings.add((String) answer);
        });
        return strings;
    }

    public Integer getPosition(String target) {
        iterationInteger = null;
        iterationInt = 0;
        getAnswers().forEach((key, answer) -> {
            if (answer.equals(target)) {
                iterationInteger = iterationInt;
            }
            iterationInt++;
        });
        return iterationInteger;
    }
}
