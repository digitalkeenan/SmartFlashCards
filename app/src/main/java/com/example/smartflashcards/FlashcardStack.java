package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cardTrees.AlphabeticalCardTree;
import com.example.smartflashcards.cardTrees.CardTreeNode;
import com.example.smartflashcards.cardTrees.QuizCardTree;
import com.example.smartflashcards.cards.AnswerCard;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.stackDetails.StackDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FlashcardStack {

    // STACKS /////////////////////////////////////////////////////////////////////////////////////
    private AlphabeticalCardTree questionCardStack;
    private AlphabeticalCardTree answerCardStack;
    private QuizCardTree quizStack;

    // holder for quizCard nodes for which the corresponding question cards no longer exist
    private ArrayList<CardTreeNode> invalidQuizNodeList;

    // viewAnswers selects if selectionCard and viewStack are to be from answerCardStack(T) or questionCardStack(F)
    private FlashcardViewFilter flashcardViewFilter;

    private Integer iterationInteger;

    FlashcardStack(StackDetails stackDetails) {
        this.questionCardStack = new AlphabeticalCardTree(stackDetails.getQuestionLocale());
        this.answerCardStack = new AlphabeticalCardTree(stackDetails.getAnswerLocale());
        this.quizStack = new QuizCardTree();
        this.invalidQuizNodeList = new ArrayList<>();
        this.flashcardViewFilter = new FlashcardViewFilter(this);
    }

    FlashcardStack(String version, StackDetails stackDetails, MyFileInputStream alphaInputStream, MyFileInputStream quizInputStream) {
        this(stackDetails);

        // load question and answer stacks
        int nextID = 0;
        if (version.equals("0.02")) {
            try {
                nextID = alphaInputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String alphaVersion = alphaInputStream.readString();
            nextID = alphaInputStream.readInt();
        }
        this.questionCardStack.setNextID(nextID);
        int numberOfCards = 0;
        if (version.equals("0.02")) {
            try {
                numberOfCards = alphaInputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            numberOfCards = alphaInputStream.readInt();
        }
        for (int cardIndex=0; cardIndex < numberOfCards; cardIndex++) {
            Integer idNumber = null;
            if (version.equals("0.02")) {
                try {
                    idNumber = alphaInputStream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                idNumber = alphaInputStream.readInt();
            }
            QuestionCard questionCard = new QuestionCard(alphaInputStream);

            this.questionCardStack.addNode(questionCard, idNumber, false); //add without optimizing

            questionCard.getAnswers().forEach((key, answer) -> {
                newAnswer((String) answer, questionCard);
            });
        }

        // load quiz stack
        this.quizStack = new QuizCardTree(version, quizInputStream);
    }


    /**
     * GETTERS
     */
    public AlphabeticalCardTree getQuestionCardStack() { //TODO: consider adding methods so that that this can be removed (no direct access to stacks)
        return this.questionCardStack;
    }

    public AlphabeticalCardTree getAnswerCardStack() { //TODO: consider adding methods so that that this can be removed (no direct access to stacks)
        return this.answerCardStack;
    }

    public QuizCardTree getQuizStack() { //TODO: consider adding methods so that that this can be removed (no direct access to stacks)
        return this.quizStack;
    }

    public FlashcardViewFilter getFlashcardViewFilter() {
        return flashcardViewFilter;
    }

    public ArrayList<CardTreeNode> getInvalidQuizNodeList() {
        // Return and clear the remove list
        ArrayList<CardTreeNode> invalidNodeList = this.invalidQuizNodeList;
        this.invalidQuizNodeList = new ArrayList<>();
        return invalidNodeList;
    }


    /**
     * ALPHABETIC STACK FUNCTIONS
     */

    public void updateStackDetails(StackDetails stackDetails) {
        this.questionCardStack.setLocale(stackDetails.getQuestionLocale());
        this.answerCardStack.setLocale(stackDetails.getAnswerLocale());
    }

    //private because this must only be called from other methods here, which update the question cards and setStackUpdated
    private Boolean newAnswer(String answer, QuestionCard questionCard) {
        // returns true if a new answer card was added
        // returns false if the answer card already existed, so it was modified to add the questionCard
        AnswerCard newAnswerCard = new AnswerCard((String) answer, questionCard);
        AnswerCard oldAnswerCard = (AnswerCard) this.answerCardStack.addNode(newAnswerCard, null,false);
        if (nonNull(oldAnswerCard)) { //nonNull here means that above add was unsuccessful due to pre-existing answer
            oldAnswerCard.addQuestion(questionCard);
            return false;
        }
        return true;
    }

    public enum AddAnswerReturnCode {
        NEW_ANSWER_LINKED,
        EXISTING_ANSWER_LINKED,
        ANSWER_ALREADY_LINKED,
        ;
    }
    public AddAnswerReturnCode addAnswerToQuestionCard(String answer, QuestionCard questionCard) {
        if (questionCard.getAnswers().containsValue(answer)) {
            return AddAnswerReturnCode.ANSWER_ALREADY_LINKED;
        }
        questionCard.addAnswer(answer);
        if (newAnswer(answer, questionCard)) {
            return AddAnswerReturnCode.NEW_ANSWER_LINKED;
        }
        return AddAnswerReturnCode.EXISTING_ANSWER_LINKED;
    }

    public Integer deleteQuestion(QuestionCard questionCard) {
        // if last/only answer card was deleted (it only had the one question),
        // returns its view position (if any)

        // NOTE: this does not remove the quiz card, because it could take a long time to find it
        //       Instead, when a quiz card comes up and the answer card can't be found, it will be removed then

        this.questionCardStack.deleteNode();

        iterationInteger = null;

        //note: no ConcurrentModificationException here because it's not deleting from itself
        questionCard.getAnswers().forEach((key, answer) -> {
            iterationInteger = deleteQuestionFromAnswerCard((String) answer, questionCard);
        });

        return iterationInteger;
    }

    //private because this must only be called from other methods here, which update the question cards and setStackUpdated
    private Integer deleteQuestionFromAnswerCard(String answer, QuestionCard questionCard) {
        // if answer card deleted (only had the one question), returns its view position (if any)

        AnswerCard answerCard = (AnswerCard) this.answerCardStack.findCard(answer, true);

        if (nonNull(answerCard)) {
            switch (answerCard.deleteQuestion(questionCard)) {
                case QUESTION_NOT_FOUND:
                    //didn't find the question in answerCard
                    return null;
                case SUCCESS:
                    //delete from answerCard successful
                    return null;
                case NO_EXTRA_QUESTIONS:
                    //questionCard is the only question in answerCard
                    Integer position = getFlashcardViewFilter().findItem();
                    this.answerCardStack.deleteNode();
                    return position;
            }
        }
        return null;
    }


    /**
     * QUIZ STACK FUNCTIONS
     */
    public void buildRandomizedQuizStack() {
        this.quizStack = new QuizCardTree(this.questionCardStack.getNodes());
    }

    public int addQuizCard(String question, int placement) {
        QuizCard quizCard = new QuizCard(question, placement, false);
        int idNumber = this.questionCardStack.getCurrentID();
        int position = this.quizStack.addNode(idNumber, quizCard, true, 0);
        return position;
    }

    public int moveQuizCard(int placement) {
        return moveQuizCard(placement, true, 0);
    }

    public int moveQuizCard(int placement, int limit) {
        return moveQuizCard(placement, false, limit);
    }

    public int moveQuizCard(int placement, boolean newCard, int limit) {
        // This moves the first card to the location number given

        //move stack's current node pointer to first card and capture that card here
        QuizCard card = (QuizCard) this.quizStack.moveToFirst().getCard();
        card.setPosition(placement);
        int idNumber = this.quizStack.getCurrentID();
        //delete stack's current node
        this.quizStack.deleteNode();
        int finalPlacement = this.quizStack.addNode(idNumber, card, newCard, limit); // not new card
        return finalPlacement;
    }

    public boolean quizCardValidate(boolean addInvalidToStack) {
        String question = getQuizStack().getCard().getCardText();
        QuestionCard questionCard = (QuestionCard)
                getQuestionCardStack().findCard(question, true);
        boolean valid = false;
        if (nonNull(questionCard)) {
            int questionID = getQuestionCardStack().getCurrentID();
            int quizID = getQuizStack().getCurrentID();
            valid = (quizID == questionID);
        }
        if (valid) {
            // Clean up any old hash keys that don't exist anymore
            // note: this does not cause a save,
            //       so it will be repeated as needed until something else causes a save
            // TODO: If quiz stack view loads too slow, disable this part for that
            //  but enable for quizMe and in quiz card review quizCardValidate with this enabled
            QuizCard quizCard = (QuizCard) getQuizStack().getCard();
            Iterator iterator = quizCard.getCorrectAnswerCount().keySet().iterator();
            while (iterator.hasNext()) {
                int key = (int) iterator.next();
                if (!questionCard.getAnswers().containsKey(key)) {
                    iterator.remove();
                }
            }
        } else if (addInvalidToStack) {
            this.invalidQuizNodeList.add(getQuizStack().getCurrentNode());
        }
        return valid;
    }

}
