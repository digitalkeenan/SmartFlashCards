package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import com.example.smartflashcards.cardTrees.BinaryCardTree;
import com.example.smartflashcards.cardTrees.CardTreeNode;
import com.example.smartflashcards.cards.FlashCard;

import java.util.ArrayList;

public class FlashcardViewFilter {
    public enum StackType {
        QUESTION,
        ANSWER,
        QUIZ,
        ;
    }

    private StackType stackType;
    private boolean filterMode;
    private String filterPattern;
    private int filterPageSize; //TODO: perhaps add a user setting for filter page size
    private ArrayList<CardTreeNode> filterNodes;
    private CardTreeNode clickForMoreNode;
    private boolean filterComplete;

    //pointer back to the flashcardStack
    private FlashcardStack flashcardStack;

    private static String clickForMore = "Click for more...";

    FlashcardViewFilter(FlashcardStack flashcardStack) {
        this.flashcardStack = flashcardStack;
        this.stackType = StackType.QUESTION;
        this.filterMode = false;
        this.filterPattern = "";
        this.filterPageSize = 2;
        this.filterNodes = new ArrayList<>();
        this.clickForMoreNode = new CardTreeNode(0, new FlashCard(clickForMore));
        this.filterComplete = false;
    }


    /**
     * SETTERS
     */
    public boolean setStackType(StackType newStackType) {
        if (newStackType == this.stackType) {
            return false;
        }
        this.stackType = newStackType;
        if (getFilterMode()) {
            startFilter();
        }
        return true;
    }
    private void setFilterPageSize(int rows) {
        this.filterPageSize = Math.max(rows, this.filterPageSize);
    }


    /**
     * GETTERS
     */
    public StackType getStackType() {
        return this.stackType;
    }

    public boolean getFilterMode() {
        return this.filterMode;
    }

    public String getFilterPattern() {
        return this.filterPattern;
    }

    public boolean getFilterComplete() {
        return filterComplete;
    }


    /**
     * FUNCTIONS
     */
    public BinaryCardTree getViewStack() {
        switch (getStackType()) {
            case QUESTION:
                return this.flashcardStack.getQuestionCardStack();
            case ANSWER:
                return this.flashcardStack.getAnswerCardStack();
            case QUIZ:
                return this.flashcardStack.getQuizStack();
        }
        return this.flashcardStack.getQuestionCardStack(); // default selection
    }

    public void clearFilter() {
        this.filterMode = false;
    }

    public void startFilter(String pattern, int pageSize) {
        setFilterPageSize(pageSize);
        this.filterPattern = pattern;
        startFilter();
    }
    private void startFilter() {
        this.filterMode = true;
        this.filterComplete = false;
        this.filterNodes = new ArrayList<>();
        addToFilterList();
    }

    public int addToFilterList(int pageSize) {
        setFilterPageSize(pageSize);
        return addToFilterList();
    }
    private int addToFilterList() {
        int itemsAdded = 0;
        while (itemsAdded < (this.filterPageSize - 1)) {
            if (addOneToFilter()) {
                itemsAdded++;
            } else {
                return itemsAdded;
            }
        }
        return itemsAdded;
    }

    private Boolean addOneToFilter() {
        // TRUE = added one
        // FALSE = filter complete or not in filter mode
        if (this.filterMode) {
            // initialize current node to the first node or the node after the last one on the list
            int lastPosition = this.filterNodes.size() - 1;
            if (lastPosition < 0) {
                getViewStack().moveToFirst();
            } else {
                getViewStack().setCurrentNode(this.filterNodes.get(lastPosition));
                getViewStack().nextSequential();
            }

            // find next pattern match (else set currentNode to null)
            getViewStack().findNextContains(this.filterPattern);
            CardTreeNode currentNode = getViewStack().getCurrentNode();
            if (stackType == StackType.QUIZ) {
                while (nonNull(currentNode) && !this.flashcardStack.quizCardValidate(true)) {
                    getViewStack().findNextContains(this.filterPattern);
                    currentNode = getViewStack().getCurrentNode();
                }
            }

            // if found one, add it to the list and return true
            if (nonNull(currentNode)) {
                if (filterComplete) {
                    this.filterNodes.add(currentNode);
                } else {
                    // if filter not complete, add before clickForMore
                    int lastIndex = filterSize() - 1;
                    this.filterNodes.add(lastIndex, currentNode);
                }
                return true;
            }

            // if no more were found, set complete and return false;
            this.filterComplete = true;
        }
        return false;
    }

    private boolean moveToPosition(int position) {
        if (getFilterMode()) {
            if (position < this.filterNodes.size()) {
                getViewStack().setCurrentNode(this.filterNodes.get(position));
                return true;
            }
            return false;
        }
        getViewStack().moveToPosition(position);
        return true;
    }

    private CardTreeNode getFilterNode(int position) {
        if (position < this.filterNodes.size()) {
            return this.filterNodes.get(position);
        }
        if (!this.filterComplete && (position == this.filterNodes.size())) {
            return this.clickForMoreNode;
        }
        return null;
    }
    public CardTreeNode getNode(int position) {
        if (getFilterMode()) {
            return getFilterNode(position);
        }
        getViewStack().moveToPosition(position);
        return getViewStack().getCurrentNode();
    }

    public FlashCard getFlashCard(int position) {
        if (moveToPosition(position)) {
            FlashCard flashCard = getViewStack().getCard();
            return flashCard;
        }
        return null;
    }

    public Integer findItem() { // before calling this, make sure currentCard is set to the target
        int position = getViewStack().getPosition();
        if (getFilterMode()) {
            // capture string before currentNode can change //TODO: does this go away when no longer storing strings here?
            String item = getViewStack().getCard(position).getCardText();
            int lastPosition = this.filterNodes.size() - 1;
            if (position <= getStackPosition(lastPosition)) {
                // position is within bounds of current list
                if (item.contains(this.filterPattern)) {
                    int index = 0;
                    while (getStackPosition(index) < position) {
                        index++;
                    }
                    return index;
                }
            }
            return null; // item either out of range or doesn't match pattern
        }
        // if not in filter mode, just return the currentCard position
        return position;
    }

    public Integer addItem() { // before calling this, make sure currentCard is set to the new one
        // returns position of new item
        int stackPosition = getViewStack().getPosition();
        String item = getViewStack().getCard(stackPosition).getCardText();
        CardTreeNode node = getViewStack().getCurrentNode();
        if (getFilterMode()) {
            int lastPosition = this.filterNodes.size() - 1;
            if (lastPosition < 0) {
                if (item.contains(this.filterPattern)) {
                    this.filterNodes.add(node);
                    return 0;
                }
            } else {
                if (stackPosition < getStackPosition(lastPosition)) {
                    // position is within bounds of current list
                    if (item.contains(this.filterPattern)) {
                        int index = 0;
                        while (getStackPosition(index) < stackPosition) {
                            index++;
                        }
                        this.filterNodes.add(index, node);
                        return index;
                    }
                } else if (filterComplete) {
                    // if no clickForMore, can add new item to end of list
                    if (item.contains(this.filterPattern)) {
                        this.filterNodes.add(node);
                        return lastPosition + 1;
                    }
                }
            }
            return null; // item either out of range or doesn't match pattern
        }
        // if not in filter mode, just return the currentCard position
        return stackPosition;
    }

    public void deleteItem(Integer position) {
        if (nonNull(position) && getFilterMode()) {
            removeItem(position);
        }
    }
    private void removeItem(int position) {
        // position must be int instead of Integer to call remove(int) instead of remove(object)
        this.filterNodes.remove((int) position);
    }

    public Integer renameQuestion(int oldPosition, String oldText, String newText, int placement) {
        // update filter and/or quizCard as needed
        Integer newPosition = null;
        switch (getStackType()) {
            case QUESTION:
                // find, rename, and resort the questionCard
                this.flashcardStack.getQuestionCardStack().rename(oldText, newText, false);
                this.flashcardStack.addQuizCard(newText, placement);
                if (getFilterMode()) {
                    removeItem(oldPosition);
                    newPosition = addItem();
                } else {
                    newPosition = this.flashcardStack.getQuestionCardStack().getPosition();
                }
                break;
            case ANSWER:
                // find, rename, and resort the questionCard
                this.flashcardStack.getQuestionCardStack().rename(oldText, newText, false);
                this.flashcardStack.addQuizCard(newText, placement);
                // return null because question position is not relevant
                break;
            case QUIZ:
                // find, rename, and resort the questionCard
                this.flashcardStack.getQuestionCardStack().rename(oldText, newText, true);
                this.flashcardStack.getQuizStack().getCard().modifyCardText(newText);
                if (getFilterMode()) {
                    removeItem(oldPosition);
                    newPosition = addItem();
                } else {
                    newPosition = oldPosition;
                }
                break;
        }
        return newPosition;
    }

    public String getString(int position) {
        if (getFilterMode()) {
            if (position < filterSize()) {
                return getFilterNode(position).getCard().getCardText();
            }
        } else {
            if (nonNull(getViewStack().getCard(position))) {
                if (stackType == StackType.QUIZ) {
                    this.flashcardStack.quizCardValidate(true);
                }
                return getViewStack().getCard().getCardText();
            }
        }
        return null;
    }

    public Integer getID(int position) {
        FlashCard flashCard = getFlashCard(position);
        if (nonNull(flashCard)) {
            Integer currentID = getViewStack().getCurrentNode().getID();
            return currentID;
        }
        return null;
    }

    public int getSize() {
        if (getFilterMode()) {
            return filterSize();
        } else {
            return getViewStack().size();
        }
    }
    private int filterSize() {
        if (filterComplete) {
            return this.filterNodes.size();
        }
        return this.filterNodes.size() + 1;
    }

    public Integer getViewPosition(CardTreeNode node) {
        // Returns null if the node is null or not in the tree
        if (getFilterMode()) {
            if (this.filterNodes.contains(node)) {
                return this.filterNodes.indexOf(node);
            }
            if (node == clickForMoreNode) {
                // return the position AFTER all the nodes in the list
                return this.filterNodes.size();
            }
            return null;
        }
        getViewStack().setCurrentNode(node);
        return getViewStack().getPosition();
    }

    // TODO: review usages of this to see if they should just pass in the node
    public Integer getStackPosition(Integer viewPosition) {
        if (nonNull(viewPosition) && getFilterMode()) {
            if (this.filterNodes.size() > viewPosition) {
                getViewStack().setCurrentNode(this.filterNodes.get(viewPosition));
                return getViewStack().getPosition();
            }
            return null;
        }
        return viewPosition;
    }
}
