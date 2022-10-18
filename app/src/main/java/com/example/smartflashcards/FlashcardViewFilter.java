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
    private Boolean filterMode;
    private String filterPattern;
    private int filterPageSize; //TODO: perhaps add a user setting for filter page size
    private ArrayList<String> filterList;
    private ArrayList<Integer> filterStackPositions;
    private CardTreeNode lastFilterNode;

    //pointer back to the flashcardStack
    private FlashcardStack flashcardStack;

    public static String more = "Click for more...";

    FlashcardViewFilter(FlashcardStack flashcardStack) {
        this.flashcardStack = flashcardStack;
        this.stackType = StackType.QUESTION;
        this.filterMode = false;
        this.filterPattern = "";
        this.filterPageSize = 2;
        this.filterList = new ArrayList<>();
        this.filterStackPositions = new ArrayList<>();
    }


    /**
     * SETTERS
     */
    public Boolean setStackType(StackType newStackType) {
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

    public Boolean getFilterMode() {
        return this.filterMode;
    }

    public String getFilterPattern() {
        return this.filterPattern;
    }

    public ArrayList<String> getFilterList() {
        return this.filterList;
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

    public void startFilter(String pattern, int pageSize) {
        setFilterPageSize(pageSize);
        this.filterPattern = pattern;
        startFilter();
    }
    public void startFilter() {
        this.filterMode = true;
        this.filterList = new ArrayList<>();
        this.filterStackPositions = new ArrayList<>();
        getViewStack().moveToFirst();
        Boolean foundOne = updateFilterList();
        while (foundOne && (getFilterList().size() < (filterPageSize - 1))) {
            foundOne = addOneToFilter();
        }
    }

    public int addToFilterList(int pageSize) {
        setFilterPageSize(pageSize);
        int itemsAdded = 0;
        while (itemsAdded < (filterPageSize - 1)) {
            if (addOneToFilter()) {
                itemsAdded++;
            } else {
                // if it didn't find another, it removed "more"
                return itemsAdded - 1;
            }
        }
        return itemsAdded;
    }

    private Boolean addOneToFilter() {
        // TRUE = added one
        // FALSE = removed "more" (or not in filter mode)
        if (this.filterMode) {
            getViewStack().setCurrentNode(this.lastFilterNode);
            getViewStack().nextSequential();
            return updateFilterList();
        }
        return false;
    }

    public void clearFilter() {
        this.filterMode = false;
    }

    private boolean updateFilterList() {
        // for start, this returns true if found one, else false
        // for add, TRUE = added one, FALSE = removed "more"
        getViewStack().findNextContains(this.filterPattern);
        this.lastFilterNode = getViewStack().getCurrentNode();
        if (stackType == StackType.QUIZ) {
            while (nonNull(this.lastFilterNode) && !this.flashcardStack.quizCardValidate(true)) {
                getViewStack().findNextContains(this.filterPattern);
                this.lastFilterNode = getViewStack().getCurrentNode();
            }
        }
        int lastIndex = this.filterList.size() - 1;
        if (nonNull(this.lastFilterNode)) {
            this.filterStackPositions.add(getViewStack().getPosition());
            if ((lastIndex > 0) && (this.filterList.get(lastIndex).equals(more))) {
                this.filterList.add(lastIndex, this.lastFilterNode.getCard().getCardText());
            } else {
                this.filterList.add(this.lastFilterNode.getCard().getCardText());
                this.filterList.add(more);
            }
            return true;
        } else {
            if ((lastIndex > 0) && (this.filterList.get(lastIndex).equals(more))) {
                this.filterList.remove(lastIndex);
            }
        }
        return false;
    }

    private void moveToPosition(int position) {
        if (getFilterMode()) {
            if (position < this.filterStackPositions.size()) {
                getViewStack().moveToPosition(this.filterStackPositions.get(position));
            }
        } else {
            getViewStack().moveToPosition(position);
        }
    }

    public CardTreeNode getNode(int position) {
        moveToPosition(position);
        CardTreeNode node = getViewStack().getCurrentNode();
        return node;
    }

    public FlashCard getFlashCard(int position) {
        moveToPosition(position);
        FlashCard flashCard = getViewStack().getCard();
        return flashCard;
    }

    public Integer findItem() { // before calling this, make sure currentCard is set to the target
        int position = getViewStack().getPosition();
        if (getFilterMode()) {
            int lastPosition = this.filterStackPositions.size() - 1;
            if (position <= this.filterStackPositions.get(lastPosition)) {
                // position is within bounds of current list
                String item = getViewStack().getCard(position).getCardText();
                if (item.contains(this.filterPattern)) {
                    int index = 0;
                    while (this.filterStackPositions.get(index) < position) {
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
        int position = getViewStack().getPosition();
        String item = getViewStack().getCard(position).getCardText();
        if (getFilterMode()) {
            int lastPosition = this.filterStackPositions.size() - 1;
            if (lastPosition < 0) {
                if (item.contains(this.filterPattern)) {
                    this.filterList.add(item);
                    this.filterStackPositions.add(position);
                    return 0;
                }
            } else {
                if (position < this.filterStackPositions.get(lastPosition)) {
                    // position is within bounds of current list
                    if (item.contains(this.filterPattern)) {
                        int index = 0;
                        while (this.filterStackPositions.get(index) < position) {
                            index++;
                        }
                        this.filterList.add(index, item);
                        this.filterStackPositions.add(index, position);
                        return index;
                    }
                } else if ((this.filterList.size() - 1) == lastPosition) { // no extra entry for "more"
                    // if no more, can add new item to end of list
                    if (item.contains(this.filterPattern)) {
                        this.filterList.add(item);
                        this.filterStackPositions.add(position);
                        return lastPosition + 1;
                    }
                }
            }
            return null; // item either out of range or doesn't match pattern
        }
        // if not in filter mode, just return the currentCard position
        return position;
    }

    public void deleteItem(Integer position) {
        if (nonNull(position)) {
            if (getFilterMode()) {
                this.filterList.remove(position);
            }
        }
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
                    this.filterList.remove(oldPosition);
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
                    this.filterList.remove(oldPosition);
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
            if (position < getFilterList().size()) {
                return getFilterList().get(position);
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
            return getFilterList().size();
        } else {
            return getViewStack().size();
        }
    }

    public Integer getViewPosition(CardTreeNode node) {
        // Returns null if the node is null or not in the tree
        getViewStack().setCurrentNode(node);
        Integer stackPosition = getViewStack().getPosition();
        if (nonNull(stackPosition) && getFilterMode()) {
            int viewPosition = this.filterStackPositions.indexOf(stackPosition);
            if (viewPosition < 0) {
                return null;
            }
            return viewPosition;
        }
        return stackPosition;
    }

    public Integer getStackPosition(Integer viewPosition) {
        if (nonNull(viewPosition) && getFilterMode()) {
            if (this.filterStackPositions.size() > viewPosition) {
                return this.filterStackPositions.get(viewPosition);
            }
            return null;
        }
        return viewPosition;
    }
}
