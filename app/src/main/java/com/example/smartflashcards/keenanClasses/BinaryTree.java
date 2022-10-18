package com.example.smartflashcards.keenanClasses;

import static java.lang.Math.abs;
import static java.util.Objects.nonNull;

import androidx.annotation.NonNull;

public class BinaryTree {

    //state variables
    private TreeNode headNode;
    private TreeNode currentNode;
    private TreeNode parentNode; //currentNode's parent: used to cover case of current moved to null
    private Boolean currentIsLeft; // True when current node is it's parent's prev node
    private int nextID;

    public BinaryTree() {
        this.headNode = null;
        this.currentNode = null;
        this.parentNode = null;
        this.currentIsLeft = false; //arbitrary non-null initialization
        this.nextID = 0;
    }

    /**
     * SETTERS
     */
    public void setCurrentNode(TreeNode node) {
        if (nodeIsInTree(node)) {
            this.currentNode = node;
        } else {
            this.currentNode = null;
        }
        getCurrentParent(); // keeping related variables up-to-date
    }

    public void setNextID(int nextID) {
        this.nextID = nextID;
    }

    /**
     * GETTERS
     */
    public TreeNode getCurrentNode() {
        return this.currentNode;
    }

    public TreeNode getHeadNode () {
        this.parentNode = null;
        return this.currentNode = this.headNode;
    }

    public TreeNode getCurrentParent () {
        if (nonNull(this.currentNode)) {
            this.parentNode = this.currentNode.getParentNode();
            if (nonNull(this.parentNode)) {
                this.currentIsLeft = (this.parentNode.getPrevNode() == this.currentNode);
            }
        } else {
            this.parentNode = null;
        }
        return this.parentNode;
    }

    public Integer getPosition () {
        // returns the numerical position of the currentNode
        if (nonNull(this.currentNode)) {
            TreeNode retainNode = this.currentNode;
            int position = this.currentNode.getNumberPrevDescendants();
            while (nonNull(this.parentNode)) {
                if (!this.currentIsLeft) {
                    // If current is right/next from its parent,
                    // then add in the parent and all its prev descendants
                    position += 1 + this.parentNode.getNumberPrevDescendants();
                }
                moveToParent();
            }
            this.currentNode = retainNode;
            getCurrentParent(); // keeping related variables up-to-date
            return position;
        }
        return null;
    }

    public int getCurrentID() {
        return this.currentNode.getID();
    }

    public int getNextID() {
        return nextID;
    }

    /**
     * FUNCTIONS
     */
    private Boolean nodeIsInTree (TreeNode node) {
        if (nonNull(node)) {
            TreeNode parentNode = node.getParentNode();
            if (nonNull(parentNode)) {
                if ((parentNode.getPrevNode() == node) || (parentNode.getNextNode() == node)) {
                    // Node is connected to its parent, so if the parent is in the tree, it is too
                    return nodeIsInTree(parentNode);
                }
            } else {
                // If parent node is null, node must be head node or it isn't in the tree
                if (getHeadNode() == node) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getAndIncrementNextID() {
        return this.nextID++;
    }

    public int size() {
        if (nonNull(this.headNode)) {
            int size = this.headNode.getNumberDescendants() + 1;
            return size;
        }
        return 0;
    }

    public TreeNode moveToPosition (int position) {
        // returns the card at that position AND puts currentNode at that position
        if ((position >= 0) && (position < size())) {
            int currentPosition = getHeadNode().getNumberPrevDescendants();
            int basePosition = 0;
            while (currentPosition != position) {
                if (currentPosition > position) {
                    currentPosition = basePosition + moveToPrev().getNumberPrevDescendants();
                } else {
                    basePosition = currentPosition + 1;
                    currentPosition = basePosition + moveToNext().getNumberPrevDescendants();
                }
            }
            return this.currentNode;
        }
        return null;
    }

    public TreeNode moveToPrev () {
        this.parentNode = this.currentNode;
        this.currentIsLeft = true;
        return this.currentNode = nonNull(this.currentNode) ? this.currentNode.getPrevNode() : null;
    }

    public TreeNode moveToNext () {
        this.parentNode = this.currentNode;
        this.currentIsLeft = false;
        return this.currentNode = nonNull(this.currentNode) ? this.currentNode.getNextNode() : null;
    }

    public TreeNode moveToParent () {
        if (nonNull(this.currentNode)) {
            this.currentNode = this.currentNode.getParentNode();
            getCurrentParent(); //update this.parentNode and this.currentIsLeft
        }
        return this.currentNode;
    }

    public TreeNode nextTopDown () {
        TreeNode childNode;
        if (nonNull(this.currentNode)) {
            if (nonNull(this.currentNode.getPrevNode())) {
                moveToPrev();
            } else if (nonNull(this.currentNode.getNextNode())) {
                moveToNext();
            } else {
                //go up until the parent's next node is not null nor is the current node
                do {
                    childNode = this.currentNode;
                    moveToParent();
                } while (nonNull(this.currentNode) && (!nonNull(this.currentNode.getNextNode()) || (this.currentNode.getNextNode()==childNode)));
                if (nonNull(this.currentNode)) {
                    moveToNext();
                }
            }
        }
        return this.currentNode;
    }

    public TreeNode moveToFirst () {
        this.getHeadNode();

        if (nonNull(this.currentNode)) {
            while (nonNull(currentNode.getPrevNode())) {
                this.moveToPrev();
            }
        }
        return this.currentNode;
    }

    public TreeNode moveToLast() {
        this.getHeadNode();

        if (nonNull(this.currentNode)) {
            while (nonNull(this.currentNode.getNextNode())) {
                this.moveToNext();
            }
        }
        return this.currentNode;
    }

    public TreeNode nextSequential () {
        if (nonNull(this.currentNode)) {
            if (nonNull(this.currentNode.getNextNode())) {
                this.moveToNext();
                while (nonNull(this.currentNode.getPrevNode())) {
                    this.moveToPrev();
                }
            } else {
                while (nonNull(this.parentNode) && !this.currentIsLeft) {
                    //if moving up from next/right, keep going up
                    this.moveToParent();
                }
                this.moveToParent();
            }
        }
        return this.currentNode;
    }

    public TreeNode previousSequential () {
        if (nonNull(this.currentNode)) {
            if (nonNull(this.currentNode.getPrevNode())) {
                this.moveToPrev();
                while (nonNull(this.currentNode.getNextNode())) {
                    this.moveToNext();
                }
            } else {
                while (nonNull(this.parentNode) && this.currentIsLeft) {
                    //if moving up from prev/left, keep going up
                    this.moveToParent();
                }
                this.moveToParent();
            }
        }
        return this.currentNode;
    }

    public void addNode (TreeNode newNode) {
        /* if this.parentNode == null, add node as head node (first node) with no children
           else adds a node below this.parentNode on side selected by this.currentIsLeft */
        newNode.setParentNode(this.parentNode);
        this.currentNode = newNode;
        if (nonNull(this.parentNode)) {
            if (this.currentIsLeft) {
                this.parentNode.setPrevNode(this.currentNode);
            } else {
                this.parentNode.setNextNode(this.currentNode);
            }
        } else {
            //if no parent, this must be the head node (first node added)
            this.headNode = this.currentNode;
        }
        //increment descendant counts for ancestors
        for (TreeNode node = this.parentNode; nonNull(node); node = node.getParentNode()) {
            node.updateNumberDescendants();
        }
    }

    public TreeNode addToEnd (TreeNode newNode) {
        /* add next sequential when building an efficient tree from the bottom up
           assumes this.currentNode points to the last node
           must at least have a head node before using this */
        if (nonNull(this.currentNode.getPrevNode())) {
            this.moveToNext();
            newNode.setParentNode(this.parentNode);
            this.currentNode = newNode;
            this.parentNode.setNextNode(this.currentNode);
        } else {
            //if at bottom, go up until parent is unbalanced or find top
            while (nonNull(this.parentNode) && (this.parentNode.getNumberPrevDescendants() == this.parentNode.getNumberNextDescendants())) {
                this.moveToParent();
            }
            if (nonNull(this.currentNode.getPrevNode()) && !nonNull(this.currentNode.getNextNode())) {
                this.moveToNext();
                newNode.setParentNode(this.parentNode);
                this.currentNode = newNode;
                this.parentNode.setNextNode(this.currentNode);
            } else {
                //found top of a balanced section (or whole balanced tree), so insert above current
                newNode.setParentNode(this.parentNode);
                this.parentNode = newNode;
                if (nonNull(this.currentNode.getParentNode())) {
                    this.currentNode.getParentNode().setNextNode(this.parentNode);
                } else {
                    this.headNode = this.parentNode;
                }
                this.parentNode.setPrevNode(this.currentNode);
                this.currentNode.setParentNode(this.parentNode);
                this.moveToParent();
                this.currentNode.updateNumberDescendants();
            }
        }
        //increment descendant counts for ancestors
        for (TreeNode node = this.parentNode; nonNull(node); node = node.getParentNode()) {
            node.updateNumberDescendants();
        }
        return this.currentNode;
    }

    public void deleteNode() {
        TreeNode leftNode;
        TreeNode rightNode;
        TreeNode selectNode;

        if (nonNull(this.currentNode)) {
            leftNode = this.currentNode.getPrevNode();
            rightNode = this.currentNode.getNextNode();
            selectNode = nonNull(leftNode) ? leftNode : rightNode;

            /* if removed node has a parent, replace removed node with node selected for promotion
               else promotion node is the new head node */
            TreeNode repairPathNode = this.currentNode.getParentNode();
            if (nonNull(repairPathNode)) {
                if (repairPathNode.getPrevNode() == this.currentNode) {
                    repairPathNode.setPrevNode(selectNode);
                } else {
                    repairPathNode.setNextNode(selectNode);
                }
            } else {
                this.headNode = selectNode;
            }

            // fix promotion node's parent
            if (nonNull(selectNode)) {
                selectNode.setParentNode(this.currentNode.getParentNode());
            }

            /* change current node to be the promotion node
               former current node is now removed */
            this.currentNode = selectNode;
            getCurrentParent(); //update this.parentNode and this.currentIsLeft

            /* shuffle down until a null node is found:
             - left (in the tree) gets right (not in the tree) as it's new next node
             - left's old next node becomes the new left node and is placed as right's new prev node
             - right's old prev node becomes the new right node (now not in the tree)
             - repeat */
            while (nonNull(leftNode) && nonNull(rightNode)) {
                //capture right node and use it to start update of descendant counts
                if (nonNull(rightNode)) {
                    repairPathNode = rightNode;
                }

                // select left's "old next node"
                selectNode = leftNode.getNextNode();

                // right becomes left's new next node
                leftNode.setNextNode(rightNode);
                rightNode.setParentNode(leftNode);

                if (nonNull(selectNode)) {
                    // set new left
                    leftNode = selectNode;

                    // select right's "old prev node"
                    selectNode = rightNode.getPrevNode();

                    // left becomes right's new prev node
                    rightNode.setPrevNode(leftNode);
                    leftNode.setParentNode(rightNode);
                }
                // if selectNode == null, shuffle is complete

                // right holds whatever remains out of the stack
                rightNode = selectNode;
            }

            // Fix descendant counts for ancestors
            while (nonNull(repairPathNode)) {
                repairPathNode.updateNumberDescendants();
                repairPathNode = repairPathNode.getParentNode();
            }
        }
    }

    public void optimizeTree (@NonNull TreeNode optiNode) {
        /*
        To fully optimize in one brute force session:
         - first make a copy of the tree in order to disassemble it while building the new one
            - prevents risking loss of data
            - allows build process to just add them in order alphabetically
         - build it up from the bottom left (earliest in the alphabet)
         - keep track of the number of levels by adding a level number (1 is on the bottom, N is the head) to the nodes
         - when parent and child don't have consecutive level numbers, insert another card

        However, trying here to just make small changes until it's optimized
         - this can allow partial optimizations while waiting for the user
         - maintain descendant counts in the nodes
         - have other routines check for unbalanced nodes, as they go and pass to this a node that has more left or right
         - swap the start node previous or next child as needed
         - old head node (along with half it's children) replaces one of the new head node's prior children,
            - which becomes the other child of what used to be the head node
         - then check the children to see which is most unbalanced, move there and repeat

         Simulation needed to see if it would ever fully optimize and if iteration counts are reasonable
         - how many levels for 100,000 cards?
         - 1+2+4+8+16+32+64+128+256+512+1024+2048+4096+8192+16384+32768 gets close with 16 levels
         - seems reasonable to keep each run of this fast enough to complete when program termination is requested

         Note that if built like the first method above, it will have an optimal number of levels
         - and therefore it will perform well
         - however, it will not be truly balanced
         - the second method pushes for true balance
         - BEWARE then that if the first method is used to build when loading from disk
            - optimization will then spend unnecessary cycles pushing for true balance
        */

        while (nonNull(optiNode.getPrevNode()) || nonNull(optiNode.getNextNode())) {
            if (optiNode.balanceFactor() > 1) { //next side at least 2 more than prev side
                //swap
                if (nonNull(optiNode.getParentNode())) {
                    // change swapping child to point to current's parent
                    optiNode.getNextNode().setParentNode(optiNode.getParentNode());
                    // change current's parent to point to swapping child
                    if (optiNode.getParentNode().getPrevNode() == optiNode) {
                        optiNode.getParentNode().setPrevNode(optiNode.getNextNode());
                    } else {
                        optiNode.getParentNode().setNextNode(optiNode.getNextNode());
                    }
                } else { // currentNode is the head node
                    optiNode.getNextNode().setParentNode(null);
                    this.headNode = optiNode.getNextNode();
                }
                // change current node's parent to be it's swapping child
                optiNode.setParentNode(optiNode.getNextNode());
                // move one of swapping child's children to be one of current node's children
                optiNode.setNextNode(optiNode.getParentNode().getPrevNode());
                // replace the swapping child's child, that current node just picked up, with current node itself
                optiNode.getParentNode().setPrevNode(optiNode);
                // set current node's new child (if any) to have current node as its parent
                if (nonNull(optiNode.getNextNode())) {
                    optiNode.getNextNode().setParentNode(optiNode);
                }
                //update descendant counts
                optiNode.updateNumberDescendants();
                optiNode.getParentNode().updateNumberDescendants();
                //if current node's new sibling has a worse balance, switch to it before continuing
                if (nonNull(optiNode.getParentNode().getNextNode())
                        && (abs(optiNode.getParentNode().getNextNode().balanceFactor()) > abs(optiNode.balanceFactor()))) {
                    optiNode = optiNode.getParentNode().getNextNode();
                }
            } else if (optiNode.balanceFactor() < -1) { //prev side at least 2 more than next side
                //swap
                if (nonNull(optiNode.getParentNode())) {
                    // change swapping child to point to current's parent
                    optiNode.getPrevNode().setParentNode(optiNode.getParentNode());
                    // change current's parent to point to swapping child
                    if (optiNode.getParentNode().getPrevNode() == optiNode) {
                        optiNode.getParentNode().setPrevNode(optiNode.getPrevNode());
                    } else {
                        optiNode.getParentNode().setNextNode(optiNode.getPrevNode());
                    }
                } else { //currentNode is the head node
                    optiNode.getPrevNode().setParentNode(null);
                    this.headNode = optiNode.getPrevNode();
                }
                // change current node's parent to be it's swapping child
                optiNode.setParentNode(optiNode.getPrevNode());
                // move one of swapping child's children to be one of current node's children
                optiNode.setPrevNode(optiNode.getParentNode().getNextNode());
                // replace the swapping child's child, that current node just picked up, with current node itself
                optiNode.getParentNode().setNextNode(optiNode);
                // set current node's new child (if any) to have current node as its parent
                if (nonNull(optiNode.getPrevNode())) {
                    optiNode.getPrevNode().setParentNode(optiNode);
                }
                //update descendant counts
                optiNode.updateNumberDescendants();
                optiNode.getParentNode().updateNumberDescendants();
                //if current node's new sibling has a worse balance, switch to it before continuing
                if (nonNull(optiNode.getParentNode().getPrevNode())) {
                    if (abs(optiNode.getParentNode().getPrevNode().balanceFactor()) > abs(optiNode.balanceFactor())) {
                        optiNode = optiNode.getParentNode().getPrevNode();
                    }
                }
            } else {
                return; // both descendants are balanced
            }
        }
        return; // not needed, but provides a place for a debug breakpoint
    }
}
