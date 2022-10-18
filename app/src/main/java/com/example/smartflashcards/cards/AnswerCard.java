package com.example.smartflashcards.cards;

import static java.util.Objects.nonNull;

import java.util.ArrayList;

public class AnswerCard extends FlashCard {
    protected ArrayList<QuestionCard> questions;

    private int iterationInt;
    private Integer iterationInteger;
    private String iterationString;

    public AnswerCard(String answer, QuestionCard questionCard) {
        super(answer);
        this.questions = new ArrayList<QuestionCard>(1);
        this.questions.add(questionCard);
    }

    public Integer getNumberQuestions () {
        return this.questions.size();
    }

    public ArrayList<QuestionCard> getQuestions() {
        return questions;
    }

    public void addQuestion (QuestionCard questionCard) {
        this.questions.add(questionCard);
    }

    public enum DeleteReturnCode {
        SUCCESS,
        NO_EXTRA_QUESTIONS,
        QUESTION_NOT_FOUND,
        ;
    }
    public DeleteReturnCode deleteQuestion (QuestionCard questionCard) {
        //note: can't use this.questions.forEach because deleting mid-loop causes "ConcurrentModificationException"
        if (nonNull(questionCard)) {
            for (int index = 0; index < this.questions.size(); index++) {
                if (this.questions.get(index) == questionCard) {
                    if (this.questions.size() == 1) {
                        return DeleteReturnCode.NO_EXTRA_QUESTIONS; // not allowed to delete the last question
                    }
                    this.questions.remove(index); //if deleting multiple in this manner, the index would have to be decremented here
                    return DeleteReturnCode.SUCCESS;
                }
            }
        }
        return DeleteReturnCode.QUESTION_NOT_FOUND;
    }

    public String getQuestionsString() {
        iterationString = "";
        iterationInt = 0;
        getQuestions().forEach(questionCard -> {
            switch (iterationInt++) {
                case 0:
                    iterationString = questionCard.getCardText();
                    break;
                case 1:
                    iterationString = "[" + iterationString + "] & [" + (String) questionCard.getCardText() + "]";
                    break;
            }
        });
        if (iterationInt > 2) {
            iterationString += " and " + (iterationInt - 2) + " more";
        }
        return iterationString;
    }

    @Override
    public ArrayList<String> reviewStrings() {
        ArrayList<String> strings = new ArrayList<>();
        getQuestions().forEach(question -> {
            strings.add(question.getCardText());
        });
        return strings;
    }

    public Integer getPosition(QuestionCard target) {
        iterationInteger = null;
        iterationInt = 0;
        getQuestions().forEach(questionCard -> {
            if (questionCard.equals(target)) {
                iterationInteger = iterationInt;
            }
            iterationInt++;
        });
        return iterationInteger;
    }
}
