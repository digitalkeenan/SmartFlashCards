package com.example.smartflashcards.keenanClasses;

import static java.util.Objects.nonNull;

public class TreeNode {

    //state variables
    private final int idNumber;
    private TreeNode prevNode;
    private TreeNode nextNode;
    private TreeNode parentNode;
    private int numberDescendants;

    public TreeNode(int idNumber) {
        this.idNumber = idNumber;
        this.prevNode = null;
        this.nextNode = null;
        this.parentNode = null;
        this.numberDescendants = 0;
    }

    public void setPrevNode (TreeNode node) {
        this.prevNode = node;
    }

    public void setNextNode (TreeNode node) {
        this.nextNode = node;
    }

    public void setParentNode (TreeNode node) {
        this.parentNode = node;
    }

    public void updateNumberDescendants() {
        this.numberDescendants = this.getNumberPrevDescendants() + this.getNumberNextDescendants();
    }

    public int getID() {
        return this.idNumber;
    }

    public TreeNode getPrevNode() {
        return this.prevNode;
    }

    public TreeNode getNextNode() {
        return this.nextNode;
    }

    public TreeNode getParentNode() {
        return this.parentNode;
    }

    public int getNumberDescendants() {
        return this.numberDescendants;
    }

    public int getNumberPrevDescendants() {
        int numberPrevDescendants;
        if (nonNull(this.prevNode)) {
            numberPrevDescendants = this.prevNode.numberDescendants + 1; // add 1 for the prevNode itself
        } else {
            numberPrevDescendants = 0;
        }
        return numberPrevDescendants;
    }

    public int getNumberNextDescendants() {
        int numberNextDescendants;

        if (nonNull(this.nextNode)) {
            numberNextDescendants = this.nextNode.numberDescendants + 1; // add 1 for the prevNode itself
        } else {
            numberNextDescendants = 0;
        }
        return numberNextDescendants;
    }

    public int balanceFactor() {
        return this.getNumberNextDescendants() - this.getNumberPrevDescendants();
    }

}
