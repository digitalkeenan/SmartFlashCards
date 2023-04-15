package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.smartflashcards.cardTrees.AlphabeticalCardTree;
import com.example.smartflashcards.cardTrees.BinaryCardTree;
import com.example.smartflashcards.cardTrees.CardTreeNode;
import com.example.smartflashcards.cards.AnswerCard;
import com.example.smartflashcards.cards.FlashCard;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.cards.QuizCard;
import com.example.smartflashcards.dialogs.DialogData;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;
import com.example.smartflashcards.stackDetails.StackDetails;

import java.util.ArrayList;
import java.util.Stack;

public class CardStackViewModel extends ViewModel {
    public NavController navController = null;

    // STACK STUFF ////////////////////////////////////////////////////////////////////////////////
    private final MutableLiveData<String> stackName = new MutableLiveData<>();
    private final MutableLiveData<String> deletedStack = new MutableLiveData<>();
    private final MutableLiveData<StackDetails> stackDetails = new MutableLiveData<>();
    private FlashcardStack flashcardStack;
    // flag all database changes for file updates
    private final MutableLiveData<Boolean> alphaStackChanged = new MutableLiveData<>();
    private final MutableLiveData<Boolean> quizStackChanged = new MutableLiveData<>();

    // STACK VIEW STUFF ///////////////////////////////////////////////////////////////////////////
    // selectionCard holds most recently added, or modified card
    private Integer selectionPosition = 0;
    private String selectionPattern = "";
    // flag view switch where list needs to be refreshed
    private final MutableLiveData<Boolean> stackSwitched = new MutableLiveData<>();
    // flag only selection changed (when other flags below are not set)
    private final MutableLiveData<Integer> stackSelectionChanged = new MutableLiveData<>();
    // provide position for notifyItemAdded
    private final MutableLiveData<Integer> stackNewItem = new MutableLiveData<>();
    // provide position for notifyItemRemoved
    private final MutableLiveData<Integer> stackDeletedItem = new MutableLiveData<>();
    // provide position for notifyItemChanged
    private final MutableLiveData<Integer> stackChangedItem = new MutableLiveData<>();
    // provide new positions for notifyItemMoved
    private Integer stackNewPosition;
    private final MutableLiveData<Integer> stackMovedItem = new MutableLiveData<>();

    // CARD EDITOR STUFF //////////////////////////////////////////////////////////////////////////
    private FlashCard reviewCard;
    // flag only selection changed (when other flags below are not set)
    private final MutableLiveData<Integer> cardSelectionChanged = new MutableLiveData<>();
    // provide position for notifyItemAdded
    private final MutableLiveData<Integer> cardNewItem = new MutableLiveData<>();
    // provide position for notifyItemRemoved
    private final MutableLiveData<Integer> cardDeletedItem = new MutableLiveData<>();
    // provide position for notifyItemChanged
    private final MutableLiveData<Integer> cardChangedItem = new MutableLiveData<>();
    // provide positions for notifyItemMoved
    private Integer cardNewPosition;
    private final MutableLiveData<Integer> cardMovedItem = new MutableLiveData<>();


    // QUIZ STUFF /////////////////////////////////////////////////////////////////////////////////
    // Quiz Card is always the top-of-queue card
    private final MutableLiveData<QuizCard> quizMeCard = new MutableLiveData<>();
    private Stack<CardTreeNode> invalidQuizNodeStack = new Stack<>();
    private final MutableLiveData<CardTreeNode> topOfInvalidQuizNodeStack = new MutableLiveData<>();

    // DIALOG STUFF ///////////////////////////////////////////////////////////////////////////////
    // dialog communication: signal dialog results back to fragments
    private final MutableLiveData<Boolean> forceCardReview = new MutableLiveData<>();
    // start a dialog (MainActivity observes this and pushes it onto the dialog stack
    private final MutableLiveData<DialogData> dialogData = new MutableLiveData<>();

    // iteration variable(s) for forEach loops
    private DialogData iterationDialogData;

    /**
     * SETTERS
     */
    // STACK STUFF ////////////////////////////////////////////////////////////////////////////////
    public void selectStack(String stack) {
        //reset everything that isn't immediately loaded by MainActivity
        setAlphaStackChanged(false); //probably not needed since only used by observers looking for sets
        setQuizStackChanged(false); //probably not needed since only used by observers looking for sets
        clearForceCardReview();
        setSelectionPosition(0);
        this.selectionPattern = "";
        //set this last so that any effects from MainActivity observe can override any of the above
        this.stackName.setValue(stack);
    }
    public void setDeletedStack(String stack) {
        this.deletedStack.setValue(stack);
    }
    public void setStackDetails (StackDetails stackDetails) {
        this.stackDetails.setValue(stackDetails);
        this.flashcardStack.updateStackDetails(stackDetails);
    }
    public void newStackDetails (Context context) {
        this.stackDetails.setValue(new StackDetails(context));
    }
    public void loadStackDetails (MyFileInputStream inputStream) {
        this.stackDetails.setValue(new StackDetails(inputStream));
    }
    public void newFlashcardStack() {
        this.flashcardStack = new FlashcardStack(getStackDetails().getValue());
    }
    public void loadFlashcardStack(String version, MyFileInputStream alphaInputStream, MyFileInputStream quizInputStream) {
        setSelectionPosition(0); // for case where the following load switches the stackType
        this.flashcardStack = new FlashcardStack(version, getStackDetails().getValue(), alphaInputStream, quizInputStream);
    }
    public void setAlphaStackChanged(Boolean updated) {
        this.alphaStackChanged.setValue(updated);
    }
    public void setQuizStackChanged(Boolean updated) {
        this.quizStackChanged.setValue(updated);
    }

    // STACK VIEW STUFF ///////////////////////////////////////////////////////////////////////////
    public void setSelectionPosition(int position) {
        this.selectionPosition = position;
    }
    public void clearStackNotifications() {
        this.stackSwitched.setValue(false);
        this.stackSelectionChanged.setValue(null);
        this.stackNewItem.setValue(null);
        this.stackDeletedItem.setValue(null);
        this.stackChangedItem.setValue(null);
        this.stackMovedItem.setValue(null);
    }

    // CARD EDITOR STUFF //////////////////////////////////////////////////////////////////////////
    public void setReviewCard(FlashCard flashCard) {
        this.reviewCard = flashCard;
    }
    public void clearCardNotifications() {
        this.cardSelectionChanged.setValue(null);
        this.cardNewItem.setValue(null);
        this.cardDeletedItem.setValue(null);
        this.cardChangedItem.setValue(null);
        this.cardMovedItem.setValue(null);
    }

    // DIALOG STUFF ///////////////////////////////////////////////////////////////////////////////
    public void clearForceCardReview() {
        this.forceCardReview.setValue(false);
    }
    public void setDialogData(DialogData dialogData) {
        this.dialogData.setValue(dialogData);
    }


    /**
     * GETTERS
     */
    // STACK STUFF ////////////////////////////////////////////////////////////////////////////////
    public LiveData<String> getStackName() {
        return this.stackName;
    }
    public LiveData<String> getDeletedStack() {
        return this.deletedStack;
    }
    public LiveData<StackDetails> getStackDetails() {
        return this.stackDetails;
    }
    public LiveData<Boolean> getAlphaStackChanged() {
        return this.alphaStackChanged;
    }
    public LiveData<Boolean> getQuizStackChanged() {
        return this.quizStackChanged;
    }

    // STACK VIEW STUFF ///////////////////////////////////////////////////////////////////////////
    public Integer getSelectionPosition() {
        return this.selectionPosition;
    }
    public LiveData<Boolean> getStackSwitched() {
        return this.stackSwitched;
    }
    public LiveData<Integer> getStackSelectionChanged() {
        return this.stackSelectionChanged;
    }
    public LiveData<Integer> getStackNewItem() {
        return this.stackNewItem;
    }
    public LiveData<Integer> getStackDeletedItem() {
        return this.stackDeletedItem;
    }
    public LiveData<Integer> getStackChangedItem() {
        return this.stackChangedItem;
    }
    public Integer getStackNewPosition() {
        return stackNewPosition;
    }
    public LiveData<Integer> getStackMovedItem() {
        return this.stackMovedItem;
    }

    // CARD EDITOR STUFF //////////////////////////////////////////////////////////////////////////
    public FlashCard getReviewCard() {
        return reviewCard;
    }
    public LiveData<Integer> getCardSelectionChanged() {
        return this.cardSelectionChanged;
    }
    public LiveData<Integer> getCardNewItem() {
        return this.cardNewItem;
    }
    public LiveData<Integer> getCardDeletedItem() {
        return this.cardDeletedItem;
    }
    public LiveData<Integer> getCardChangedItem() {
        return this.cardChangedItem;
    }
    public Integer getCardNewPosition() {
        return cardNewPosition;
    }
    public LiveData<Integer> getCardMovedItem() {
        return this.cardMovedItem;
    }

    // QUIZ STUFF /////////////////////////////////////////////////////////////////////////////////
    public LiveData<QuizCard> getQuizMeCard() {
        return this.quizMeCard;
    }
    public LiveData<CardTreeNode> getTopOfInvalidQuizNodeStack() {
        return this.topOfInvalidQuizNodeStack;
    }

    // DIALOG STUFF ///////////////////////////////////////////////////////////////////////////////
    public LiveData<Boolean> getForceCardReview() {
        return this.forceCardReview;
    }
    public LiveData<DialogData> getDialogData() {
        return this.dialogData;
    }

    /**
     * ALPHABETIC STACK FUNCTIONS
     */
    public void saveAlphabeticalStack(MyFileOutputStream outputStream) {
        this.flashcardStack.getQuestionCardStack().writeFile(outputStream);
    }

    //NOTE: fragments/activities that call this must observe forceCardReview in CardStackViewModel
    // and at least clear it when it sets
    // because when a card is modified rather than added,
    // forceCardReview may be set by the ProposeCardReview dialog
    public void addQuestionCard(String question, String answer, AnswerCard answerCard, int placement) {
        addQuestionCard(question, answer, answerCard, placement, true);
    }
    public void addQuestionCard(String question, String answer, AnswerCard answerCard, int placement, boolean userAdded) {
        if (nonNull(question) && !question.equals("") && nonNull(answer) && !answer.equals("")) {
            QuestionCard newCard = new QuestionCard(question, answer);
            QuestionCard questionCard = (QuestionCard) this.flashcardStack.getQuestionCardStack().
                    addNode(newCard, null, true);
            if (nonNull(questionCard)) {
                // nonNull questionCard already existed
                if (nonNull(answerCard)) {
                    // From answer review of its current questions - add link to pre-existing question
                    answerCard.addQuestion(questionCard);
                    questionCard.addAnswer(answer);
                    this.cardNewItem.setValue(answerCard.getNumberQuestions() - 1);
                    setAlphaStackChanged(true);
                } else {
                    // Found card already existed, so didn't add it, but set default selection there
                    if (getStackType() == FlashcardViewFilter.StackType.QUESTION) {
                        // From plus button on question list
                        setStackSelectionChanged();
                    }
                    // attempt to add new answer
                    boolean successful = addAnswerToQuestionCard(answer, questionCard);
                    if (userAdded) {
                        if (successful) {
                            DialogData dialogData = new DialogData(DialogData.Type.RECOMMEND_REVIEW, DialogData.Action.setForceCardReview);
                            String message = "Your " + getStackDetails().getValue().getAnswerLabel();
                            message += " was added as an additional valid response for that pre-existing ";
                            message += getStackDetails().getValue().getQuestionLabel();
                            message += "\n\nIt is recommended to review the flashcard to ensure that the new valid response is unique";
                            dialogData.setMessage(message);
                            dialogData.setFlashCard(questionCard); // proposing to review the answers for this questionCard
                            setDialogData(dialogData);
                        } else {
                            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
                            dialogData.setMessage("That exact flashcard already exists");
                            setDialogData(dialogData);
                        }
                    }
                }
            } else {
                // added new card
                if (nonNull(answerCard)) {
                    // From existing answer review of it's current questions, added new question
                    answerCard.addQuestion(newCard);
                    this.cardNewItem.setValue(answerCard.getNumberQuestions() - 1);
                } else {
                    if (getStackType() == FlashcardViewFilter.StackType.QUESTION) {
                        // From plus button on question list
                        setStackNewItem();
                    }
                    AnswerCard newAnswerCard = new AnswerCard((String) answer, newCard);
                    AnswerCard oldAnswerCard = (AnswerCard) this.flashcardStack.getAnswerCardStack().
                            addNode(newAnswerCard, null, false);
                    if (nonNull(oldAnswerCard)) {
                        // nonNull here means that above add was unsuccessful due to pre-existing answer
                        oldAnswerCard.addQuestion(newCard);
                        if (getStackType() == FlashcardViewFilter.StackType.ANSWER) {
                            setStackSelectionChanged();
                        }
                        if (userAdded) {
                            DialogData dialogData = new DialogData(DialogData.Type.RECOMMEND_REVIEW, DialogData.Action.setForceCardReview);
                            String message = "Your " + getStackDetails().getValue().getQuestionLabel();
                            message += " was added as an additional valid flashcard for that pre-existing ";
                            message += getStackDetails().getValue().getAnswerLabel();
                            message += "\n\nIt is recommended to review the flashcards for that response to ensure that this new ";
                            message += getStackDetails().getValue().getQuestionLabel() + " is unique";
                            dialogData.setMessage(message);
                            dialogData.setFlashCard(oldAnswerCard); // proposing to review the questions for this answerCard
                            setDialogData(dialogData);
                        }
                    } else {
                        // new answer card
                        if (getStackType() == FlashcardViewFilter.StackType.ANSWER) {
                            // From plus button on answer list
                            setStackNewItem();
                        }
                    }
                }

                // add quiz card
                int finalPlacement = this.flashcardStack.addQuizCard(newCard.getCardText(), placement);
                if (finalPlacement == 0) { // if on top of stack
                    updateQuizMeCard();
                }
                if (getStackType() == FlashcardViewFilter.StackType.QUIZ) {
                    setStackNewItem();
                }

                setAlphaStackChanged(true);
                setQuizStackChanged(true);
            }
        }
    }

    public Boolean addAnswerToQuestionCard(String answer, QuestionCard questionCard) {
        // returns true if successful
        // returns false if answer id blank or already exists in this questionCard
        if (nonNull(answer) && !answer.equals("")) {
            FlashcardStack.AddAnswerReturnCode answerAdded = this.flashcardStack.addAnswerToQuestionCard(answer, questionCard);
            switch (answerAdded) {
                case NEW_ANSWER_LINKED:
                    this.cardNewItem.setValue(questionCard.getAnswers().size() - 1);
                    setStackNewItem();
                    setAlphaStackChanged(true);
                    return true;
                case EXISTING_ANSWER_LINKED:
                    this.cardNewItem.setValue(questionCard.getAnswers().size() - 1);
                    setStackSelectionChanged();
                    setAlphaStackChanged(true);
                    return true;
                case ANSWER_ALREADY_LINKED:
                    this.cardSelectionChanged.setValue(questionCard.getPosition(answer));
                    this.flashcardStack.getAnswerCardStack().findCard(answer, false);
                    setStackSelectionChanged();
                    return false;
            }
        }
        return false;
    }

    public void deleteAnswer(String answer) {
        // run deleteAnswerFromQuestionCard on each question found in the AnswerCard for answer
        AnswerCard answerCard = (AnswerCard) this.flashcardStack.getAnswerCardStack().findCard(answer, true);

        if (nonNull(answerCard)) {
            // Avoid ConcurrentModificationException by first copying the list
            ArrayList<QuestionCard> questionCardArrayList = new ArrayList<QuestionCard>(1);
            answerCard.getQuestions().forEach(questionCard -> {
                questionCardArrayList.add(questionCard);
            });
            questionCardArrayList.forEach(questionCard -> {
                //TODO: can the prompt also have a yes-to-all button?
                // note that this currently will result in file updates after each delete
                // - if a yes-to-all option is added, it would be best to wait until done before updating files
                // - perhaps defer all file updates until views are updated or changed
                // - this seems dangerous / too easy to forget to call the file update on a view update
                iterationDialogData = new DialogData(DialogData.Type.CONTINUE_OR_CANCEL, DialogData.Action.deleteAnswerFromQuestionCard);
                iterationDialogData.setMessage("Are you sure that you want to delete ["
                        + answer + "] from the flashcard for [" + questionCard.getCardText() + "]?");
                iterationDialogData.setDataString(answer);
                iterationDialogData.setFlashCard(questionCard);
                setDialogData(iterationDialogData);
            });
        }
    }

    public void deleteAnswerFromQuestionCard(String answer, QuestionCard questionCard, boolean fromQuestionCardReview) {
        if (nonNull(questionCard)) {
            Integer position = questionCard.getPosition(answer);
            switch (questionCard.deleteAnswer(answer)) {
                case ANSWER_NOT_FOUND:
                    //didn't find the answer in questionCard
                    break;
                case SUCCESS:
                    //delete from questionCard successful
                    if (fromQuestionCardReview) {
                        this.cardDeletedItem.setValue(position);
                    }
                    deleteQuestionFromAnswerCard(answer, questionCard, fromQuestionCardReview);
                    setAlphaStackChanged(true);
                    break;
                case NO_EXTRA_ANSWERS:
                    //answer to delete was the only one in questionCard
                    DialogData dialogData = new DialogData(
                            DialogData.Type.CONTINUE_OR_CANCEL,
                            DialogData.Action.deleteQuestionFromAnswerCardReview);
                    dialogData.setFlashCard(questionCard);
                    dialogData.setDataString(answer);

                    String message = "This " + getStackDetails().getValue().getQuestionLabel();
                    message += " [" + questionCard.getCardText() + "]";
                    message += "\nhas only one " + getStackDetails().getValue().getAnswerLabel();
                    message += " [" + answer + "]";
                    message += "\n\nClick CONTINUE to remove the entire " + getStackDetails().getValue().getQuestionLabel();
                    dialogData.setMessage(message);

                    setDialogData(dialogData);
                    break;
            }
        }
    }

    public void deleteQuestionFromAnswerCardReview(String question, String answer) {
        // for confirmed delete from above dialog command

        // sets currentNode in questionCardStack
        QuestionCard questionCard = (QuestionCard) this.flashcardStack.getQuestionCardStack().findCard(question, true);

        Integer position = questionCard.getPosition(answer);
        this.flashcardStack.deleteQuestion(questionCard);
        this.cardDeletedItem.setValue(position);
        setAlphaStackChanged(true);
    }

    // private because this must only be called from other methods here,
    // which update the question cards and setAlphaStackUpdated and setQuizStackUpdated
    private void deleteQuestionFromAnswerCard(String answer, QuestionCard questionCard, boolean fromQuestionCardReview) {
        AnswerCard answerCard = (AnswerCard) this.flashcardStack.getAnswerCardStack().findCard(answer, true);
        Integer position = answerCard.getPosition(questionCard);

        if (nonNull(answerCard)) {
            switch (answerCard.deleteQuestion(questionCard)) {
                case QUESTION_NOT_FOUND:
                    break;
                case SUCCESS:
                    if (!fromQuestionCardReview) { // possibly from answer card review
                        this.cardDeletedItem.setValue(position);
                    }
                    break;
                case NO_EXTRA_QUESTIONS:
                    // delete the answer card
                    if (getStackType() == FlashcardViewFilter.StackType.ANSWER) {
                        position = this.flashcardStack.getFlashcardViewFilter().findItem();
                        this.flashcardStack.getFlashcardViewFilter().deleteItem(position);
                        this.flashcardStack.getAnswerCardStack().deleteNode();
                        this.stackDeletedItem.setValue(position);
                    } else {
                        this.flashcardStack.getAnswerCardStack().deleteNode();
                    }
                    break;
            }
        }
    }

    public void deleteQuestion(String question) {
        // Delete selection from list of question cards or list of quiz cards

        //NOTE: If not in quiz mode, this does not remove the quiz card,
        //           because it could take a long time to find it
        //      Instead, when a quiz card comes up and the answer card can't be found,
        //           it will be removed then

        QuestionCard questionCard = (QuestionCard) this.flashcardStack.getQuestionCardStack().findCard(question, true);
        if (nonNull(questionCard)) {
            this.flashcardStack.deleteQuestion(questionCard);
            setAlphaStackChanged(true);
            if (getStackType() == FlashcardViewFilter.StackType.QUIZ) {
                this.flashcardStack.getFlashcardViewFilter().deleteViewCard(getSelectionPosition());
                setQuizStackChanged(true);
            }
            this.flashcardStack.getFlashcardViewFilter().deleteItem(getSelectionPosition());
            this.stackDeletedItem.setValue(getSelectionPosition());
        }
    }

    public QuestionCard findQuestionCard(String question) {
        return (QuestionCard) this.flashcardStack.getQuestionCardStack().findCard(question, true);
    }

    public void modifyQuestionCard(String oldText, String newText, int placement) {
        if (nonNull(this.flashcardStack.getQuestionCardStack().findCard(newText, true))) {
            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
            dialogData.setMessage("Sorry! Can't change to [" + newText + "] because that flashcard already exists");
            setDialogData(dialogData);
        } else {
            // set currentNode in questionCardStack or quizCardStack
            this.flashcardStack.getFlashcardViewFilter().moveToPosition(getSelectionPosition());

            Integer oldPosition = this.flashcardStack.getFlashcardViewFilter().findItem();
            if (nonNull(oldPosition)) {
                this.cardChangedItem.setValue(oldPosition);
                Integer newPosition = this.flashcardStack.getFlashcardViewFilter().renameQuestion(oldPosition, oldText, newText, placement);
                if (nonNull(newPosition)) {
                    if (newPosition == oldPosition) {
                        this.stackChangedItem.setValue(oldPosition);
                    } else {
                        this.stackNewPosition = newPosition;
                        this.stackMovedItem.setValue(oldPosition);
                    }
                } else {
                    // filter mode is on and new text doesn't match filter pattern
                    this.stackDeletedItem.setValue(oldPosition);
                }
            }
            setAlphaStackChanged(true);
            setQuizStackChanged(true);
            if (placement == 0) { // if on top of stack
                updateQuizMeCard();
            }
        }
    }

    public void modifyAnswer(QuestionCard questionCard, String oldText, String newText) {
        // modify answer in this one questionCard
        if (nonNull(newText) && !newText.equals("") && !newText.equals(oldText)) {
            Boolean answerAdded = addAnswerToQuestionCard(newText, questionCard);
            int oldPosition = questionCard.getPosition(oldText);
            questionCard.deleteAnswer(oldText);
            int newPosition = questionCard.getAnswers().size() - 1;
            deleteQuestionFromAnswerCard(oldText, questionCard, false);
            if (answerAdded) {
                if (newPosition == oldPosition) {
                    this.cardChangedItem.setValue(oldPosition);
                } else {
                    this.cardNewPosition = newPosition;
                    this.cardMovedItem.setValue(oldPosition);
                }
            } else {
                this.cardDeletedItem.setValue(oldPosition);
            }
            setAlphaStackChanged(true);
        }
    }

    public void modifyAnswerCard(AnswerCard answerCard, String newText) {
        //modify answer in every question its in
        if (nonNull(newText) && !newText.equals("")) {
            String oldText = answerCard.getCardText();
            answerCard.getQuestions().forEach(questionCard -> {
                this.flashcardStack.addAnswerToQuestionCard(newText, questionCard);
                questionCard.deleteAnswer(oldText);
            });

            // find and delete old card
            this.flashcardStack.getAnswerCardStack().findCard(oldText, true);
            Integer oldPosition = this.flashcardStack.getFlashcardViewFilter().findItem();
            this.flashcardStack.getFlashcardViewFilter().deleteItem(oldPosition);
            this.flashcardStack.getAnswerCardStack().deleteNode();

            // find and add new card
            this.flashcardStack.getAnswerCardStack().findCard(newText, true);
            Integer newPosition = this.flashcardStack.getFlashcardViewFilter().addItem();

            // notify
            if (nonNull(newPosition)) {
                if (oldPosition == newPosition) {
                    this.stackChangedItem.setValue(oldPosition);
                } else {
                    this.stackNewPosition = newPosition;
                    this.stackMovedItem.setValue(oldPosition);
                }
            } else {
                // modified answer is not on this list or was already on the list
                this.stackDeletedItem.setValue(oldPosition);
                // TODO: if changed to one already on the list, also select that
            }
            setAlphaStackChanged(true);
        }
    }

    /**
     * QUIZ FUNCTIONS
     */
    public int getQuizSize() {
        return this.flashcardStack.getQuizStack().size();
    }

    public int getGoldStars() {
        return this.flashcardStack.getQuizStack().getGoldStars();
    }

    public int getSilverStars() {
        return this.flashcardStack.getQuizStack().getSilverStars();
    }

    public int incrementGoldStars() {
        return this.flashcardStack.getQuizStack().incrementGoldStars();
    }

    public int incrementSilverStars() {
        return this.flashcardStack.getQuizStack().incrementSilverStars();
    }

    public int decrementGoldStars() {
        return this.flashcardStack.getQuizStack().decrementGoldStars();
    }

    public int decrementSilverStars() {
        return this.flashcardStack.getQuizStack().decrementSilverStars();
    }

    public ArrayList<Integer> getQuizStats() {
        return this.flashcardStack.getQuizStack().getStats();
    }

    public void saveQuizStack(MyFileOutputStream outputStream) {
        this.flashcardStack.getQuizStack().writeFile(outputStream);
    }

    public void buildRandomizedQuizStack() {
        this.flashcardStack.buildRandomizedQuizStack();
        setQuizStackChanged(true);
        updateQuizMeCard();
        this.stackSwitched.setValue(true);
    }

    public void updateQuizMeCard() {
        CardTreeNode firstNode = this.flashcardStack.getQuizStack().moveToFirst();
        while (nonNull(firstNode)) {
            if (quizCardValidate()) {
                this.quizMeCard.setValue((QuizCard)firstNode.getCard());
                return;
            }
            // question card was deleted
            // finding and deleting quiz card at that point might take too long, so do it now
            this.flashcardStack.getQuizStack().deleteNode();
            setQuizStackChanged(true);
            firstNode = this.flashcardStack.getQuizStack().moveToFirst();
        }
        this.quizMeCard.setValue(null);
    }

    public QuizCard moveQuizMeCard(boolean correct, int adjustor, int limit) {
        QuizCard card = this.flashcardStack.moveQuizMeCard(correct, adjustor, limit);
        updateQuizMeCard();
        setQuizStackChanged(true);
        return card;
    }


    public int moveQuizCard(QuizCard quizCard, int position, boolean clearStats) {
        // This moves the card to the location number given
        int finalPlacement = this.flashcardStack.moveQuizCard(quizCard, position, clearStats);
        updateQuizMeCard();
        //TODO: add notify-moved
        setQuizStackChanged(true);
        return finalPlacement;
    }

    public void deleteQuizCard(CardTreeNode invalidNode) {
        // an invalid node will get queued for deletion multiple times, so ignore if already deleted
        if (this.flashcardStack.getQuizStack().setCurrentNode(invalidNode)) {
            Integer position = this.flashcardStack.getFlashcardViewFilter().findItem();
            this.flashcardStack.getQuizStack().deleteNode();
            setQuizStackChanged(true);
            if (nonNull(position)) {
                this.flashcardStack.getFlashcardViewFilter().deleteItem(position);
                this.stackDeletedItem.setValue(position);
            }
        }
    }

    public void pushInvalidQuizCardStack(ArrayList<CardTreeNode> invalidQuizCards) {
        invalidQuizCards.forEach(quizNode -> {
            this.invalidQuizNodeStack.push(quizNode);
        });
        if (!nonNull(this.topOfInvalidQuizNodeStack.getValue())) {
            popInvalidQuizCardStack();
        }
    }

    public boolean quizCardValidate() {
        return this.flashcardStack.quizCardValidate(false);
    }

    public void popInvalidQuizCardStack() {
        if (this.invalidQuizNodeStack.empty()) {
            this.topOfInvalidQuizNodeStack.setValue(null);
        } else {
            CardTreeNode invalidNode = this.invalidQuizNodeStack.pop();
            this.topOfInvalidQuizNodeStack.setValue(invalidNode);
        }
    }

    /**
     * VIEW STACK FUNCTIONS
     */
    public void setStackType(FlashcardViewFilter.StackType stackType) {
        if (this.flashcardStack.getFlashcardViewFilter().setStackType(stackType)) {
            pushInvalidQuizCardStack(this.flashcardStack.getInvalidQuizNodeList());
            setSelectionPosition(0);
            this.stackSwitched.setValue(true);
        }
    }

    public void setForceCardReview(FlashCard flashCard) {
        setReviewCard(flashCard);
        this.forceCardReview.setValue(true);
    }

    public FlashcardViewFilter.StackType getStackType() {
        return this.flashcardStack.getFlashcardViewFilter().getStackType();
    }
    public BinaryCardTree getViewStack() {
        return this.flashcardStack.getFlashcardViewFilter().getViewStack();
    }
    public FlashCard getSelectionCard() {
        Integer position = getSelectionPosition();
        if (nonNull(position)) {
            FlashCard selectionCard = this.flashcardStack.getFlashcardViewFilter().getFlashCard(position);
            return selectionCard;
        }
        return null;
    }
    public Integer getSelectionStackPosition() {
        Integer position = getSelectionPosition();
        return this.flashcardStack.getFlashcardViewFilter().getStackPosition(position);
    }

    public Boolean getFilterMode() {
        return this.flashcardStack.getFlashcardViewFilter().getFilterMode();
    }
    public void startFilter (String pattern, int pageSize) {
        this.flashcardStack.getFlashcardViewFilter().startFilter(pattern, pageSize);
        pushInvalidQuizCardStack(this.flashcardStack.getInvalidQuizNodeList());
    }
    public int addToFilterList (int pageSize) {
        int itemsAdded = this.flashcardStack.getFlashcardViewFilter().addToFilterList(pageSize);
        pushInvalidQuizCardStack(this.flashcardStack.getInvalidQuizNodeList());
        return itemsAdded;
    }
    public void clearFilter () {
        this.flashcardStack.getFlashcardViewFilter().clearFilter();
    }
    public boolean getViewFilterComplete() {
        return this.flashcardStack.getFlashcardViewFilter().getFilterComplete();
    }

    public String getPattern() {
        if (getFilterMode()) {
            return this.flashcardStack.getFlashcardViewFilter().getFilterPattern();
        }
        return this.selectionPattern;
    }

    public int getViewSize() {
        return this.flashcardStack.getFlashcardViewFilter().getSize();
    }

    public CardTreeNode getViewNode(Integer position) {
        return this.flashcardStack.getFlashcardViewFilter().getNode(position);
    }

    public Integer getViewPosition(CardTreeNode node) {
        return this.flashcardStack.getFlashcardViewFilter().getViewPosition(node);
    }

    public String getViewCardString(Integer position) {
        String viewCardString = this.flashcardStack.getFlashcardViewFilter().getString(position);
        pushInvalidQuizCardStack(this.flashcardStack.getInvalidQuizNodeList());
        return viewCardString;
    }

    public Integer findCardStartsWith (String pattern) {
        this.selectionPattern = pattern;
        Integer position = ((AlphabeticalCardTree) getViewStack()).findCardStartsWith(pattern, true);
        return position;
    }

    private void setStackSelectionChanged() {
        Integer position = this.flashcardStack.getFlashcardViewFilter().findItem();
        if (nonNull(position)) {
            this.stackSelectionChanged.setValue(position);
        } // else in filter mode, but question is either out of range or doesn't match pattern
    }

    private void setStackNewItem() {
        Integer position = this.flashcardStack.getFlashcardViewFilter().addItem();
        if (nonNull(position)) {
            this.stackNewItem.setValue(position);
        } // else in filter mode, but item is either out of range or doesn't match pattern
    }

    /**
     * DIALOG FUNCTIONS
     */
    public void createNewFlashcardDialog(FlashCard answerCard) {
        //non-null answerCard value provided when adding another question for a pre-existing answer
        DialogData dialogData;
        if (nonNull(answerCard)) {
            dialogData = new DialogData(DialogData.Type.ENTER_NEW_QUESTION, DialogData.Action.addQuestionCard);
        } else {
            dialogData = new DialogData(DialogData.Type.CREATE_NEW_FLASHCARD, DialogData.Action.addQuestionCard);
        }
        dialogData.setFlashCard(answerCard);
        dialogData.setMessage("Enter text for the flashcard");
        setDialogData(dialogData);
    }

    public void createAddAnswerDialog(QuestionCard questionCard) {
        DialogData dialogData = new DialogData(DialogData.Type.ENTER_NEW_ANSWER, DialogData.Action.addAnswerToQuestionCard);
        dialogData.setFlashCard(questionCard);
        dialogData.setMessage("Enter text for " + this.stackDetails.getValue().getAnswerLabel());
        setDialogData(dialogData);
    }

    public void createModifyQuestionDialog(String question) {
        createModifyQuestionDialog(question, null);
    }
    public void createModifyQuestionDialog(String question, Integer placement) {
        // if placement is provided (nonNull), lock it
        DialogData dialogData = new DialogData(DialogData.Type.EDIT_CARD_TEXT, DialogData.Action.modifyQuestionCard);
        dialogData.setFlashCard(this.flashcardStack.getQuestionCardStack().findCard(question, true));
        dialogData.setInteger(placement);
        dialogData.setMessage("Modify text for " + this.stackDetails.getValue().getQuestionLabel());
        setDialogData(dialogData);
    }

    public void createModifyAnswerDialog(String answer) {
        // modify the answer for every question it's in
        DialogData dialogData = new DialogData(DialogData.Type.EDIT_CARD_TEXT, DialogData.Action.modifyAnswerCardAll);
        dialogData.setFlashCard(this.flashcardStack.getAnswerCardStack().findCard(answer, true));
        dialogData.setMessage("Modify text for " + this.stackDetails.getValue().getAnswerLabel());
        setDialogData(dialogData);
    }

    public void createModifyAnswerDialog(String answer, QuestionCard questionCard) {
        // modify the answer only for this QuestionCard
        DialogData dialogData = new DialogData(DialogData.Type.EDIT_CARD_TEXT, DialogData.Action.modifyAnswerCardOne);
        dialogData.setDataString(answer);
        dialogData.setFlashCard(questionCard);
        String message = "Modify text for " + this.stackDetails.getValue().getAnswerLabel();
        // if the answer is attached to other questions, append warning message
        AnswerCard answerCard = (AnswerCard) this.flashcardStack.getAnswerCardStack().findCard(answer, true);
        int numberExtraQuestions = answerCard.getNumberQuestions() - 1;
        if (numberExtraQuestions > 0) {
            message += "\n\nWARNING: this " + this.stackDetails.getValue().getAnswerLabel();
            message += " is also attached to " + numberExtraQuestions + " other flashcard";
            if (numberExtraQuestions > 1) {
                message += "s\nTo change them all, ";
            } else {
                message += "\nTo change them both, ";
            }
            message += "modify it from the review/edit flashcards screen (switch view)";
        }
        dialogData.setMessage(message);
        setDialogData(dialogData);
    }

    public void createDeleteQuestionDialog(String question) {
        QuestionCard questionCard = (QuestionCard) this.flashcardStack.getQuestionCardStack().findCard(question, true);
        if (nonNull(questionCard)) {
            DialogData dialogData = new DialogData(DialogData.Type.CONTINUE_OR_CANCEL, DialogData.Action.deleteQuestion);

            String message = "Are you sure that you want to delete this ";
            message += getStackDetails().getValue().getQuestionLabel() + ":\n[" + question;
            message += "]\n\nIt's " + getStackDetails().getValue().getAnswerLabel();

            if (questionCard.getAnswers().size() > 1) {
                message += " responses are " + questionCard.getAnswersString();
            } else {
                message += " is [" + questionCard.getAnswersString() + "]";
            }

            dialogData.setMessage(message);
            dialogData.setDataString(question);
            setDialogData(dialogData);
        }
    }
}
