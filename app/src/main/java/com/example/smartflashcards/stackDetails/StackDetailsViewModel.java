package com.example.smartflashcards.stackDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StackDetailsViewModel extends ViewModel {
    private final MutableLiveData<String> stackSelection = new MutableLiveData<>();
    private final MutableLiveData<String> stackName = new MutableLiveData<>();
    private final MutableLiveData<StackDetails> stackDetails = new MutableLiveData<>();

    // Main Activity to trigger on set of this and take action if it's set to the same as the active stack
    private final MutableLiveData<String> stackDetailsUpdated = new MutableLiveData<>();

    /**
     * SETTERS
     */
    public void setStackSelection(String stack) {
        this.stackSelection.setValue(stack);
    }
    public void selectStack(String stack) {
        setStackSelection(stack);
        this.stackName.setValue(stack);
        this.stackDetails.setValue(null); //clear details; stackDetailsFragment will load them
    }
    public void setStackDetails(StackDetails stackDetails) {
        this.stackDetails.setValue(stackDetails);
    }
    public void setStackDetailsUpdated(String stack) {
        this.stackDetailsUpdated.setValue(stack);
    }

    /**
     * GETTERS
     */
    public LiveData<String> getStackSelection() {
        return this.stackSelection;
    }
    public LiveData<String> getStackName() {
        return this.stackName;
    }
    public LiveData<StackDetails> getStackDetails() {
        return this.stackDetails;
    }
    public LiveData<String> getStackDetailsUpdated() {
        return this.stackDetailsUpdated;
    }

    /**
     * FUNCTIONS
     */
}
