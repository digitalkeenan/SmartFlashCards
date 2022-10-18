package com.example.smartflashcards.stackDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StackDetailsViewModel extends ViewModel {
    private final MutableLiveData<String> stackName = new MutableLiveData<>();
    private final MutableLiveData<StackDetails> stackDetails = new MutableLiveData<>();

    // Main Activity to trigger on set of this and take action if it's set to the same as the active stack
    private final MutableLiveData<String> stackDetailsUpdated = new MutableLiveData<>();

    /**
     * SETTERS
     */
    public void selectStack(String stack) {
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
    public LiveData<String> getStackName() {
        return stackName;
    }
    public LiveData<StackDetails> getStackDetails() {
        return stackDetails;
    }
    public LiveData<String> getStackDetailsUpdated() {
        return stackDetailsUpdated;
    }

    /**
     * FUNCTIONS
     */
}
