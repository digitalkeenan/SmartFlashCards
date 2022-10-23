package com.example.smartflashcards.dialogs;

import com.example.smartflashcards.cards.FlashCard;

public class DialogData {

    /**
     * ENUMERATIONS
     */
    public enum Type {
        CONTINUE ("Information"),
        CONTINUE_OR_CANCEL ("Continue or Cancel"),
        CREATE_NEW_FLASHCARD ("Create new Flashcard"),
        CREATE_NEW_STACK ("Create new Stack"),
        EDIT_CARD_TEXT ("Edit card text"),
        ENTER_NEW_QUESTION ("Enter new Question"),
        ENTER_NEW_ANSWER ("Enter new Answer"),
        RECOMMEND_REVIEW("Review recommended"),
        REMOVE_CHANGE_CANCEL ("Remove or Change"),
        ;
        private final String string;
        Type(String string) {
            this.string = string;
        }
        @Override
        public String toString() {
            return string;
        }
    }

    public enum Action {
        addAnswerToQuestionCard,
        addQuestionCard,
        addStack,
        deleteAnswerFromQuestionCard,
        deleteQuestionFromAnswerCardReview,
        deleteQuestion,
        deleteStack,
        modifyAnswerCardAll,
        modifyAnswerCardOne,
        modifyQuestionCard,
        noAction,
        setForceCardReview,
        ;
    }

    /**
     * VARIABLES
     */
    private Type type;
    private Action action;
    private String message;
    private String dataString;
    private Integer integer;
    private FlashCard flashCard = null;

    /**
     * CONSTRUCTOR
     */
    public DialogData (Type type, Action action) {
        this.type = type;
        this.action = action;
        this.message = "";
        this.dataString = "";
    }

    /**
     * SETTERS
     */
    public void setMessage (String message) {
        this.message = message;
    }
    public void setDataString (String dataString) {
        this.dataString = dataString;
    }
    public void setInteger(Integer integer) {
        this.integer = integer;
    }
    public void setFlashCard (FlashCard flashCard) {
        this.flashCard = flashCard;
    }

    /**
     * GETTERS
     */
    public Type getType() {
        return this.type;
    }
    public Action getAction() {
        return this.action;
    }
    public String getMessage() {
        return this.message;
    }
    public String getDataString() {
        return this.dataString;
    }
    public Integer getInteger() {
        return this.integer;
    }
    public FlashCard getFlashCard() {
        return this.flashCard;
    }
}
