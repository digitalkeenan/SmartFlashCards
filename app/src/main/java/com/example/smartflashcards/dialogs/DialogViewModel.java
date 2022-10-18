package com.example.smartflashcards.dialogs;

import static java.util.Objects.nonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Stack;

public class DialogViewModel extends ViewModel {

    private final MutableLiveData<DialogData> topOfDialogStack = new MutableLiveData<>();

    //TODO: if this holds well in viewModel (surviving screen rotation, etc.),
    // there are probably other data in other viewModels that don't need to be LiveData
    // - i.e. anything that isn't "observed"
    private Stack<DialogData> dialogStack = new Stack<DialogData>();
    private DialogData iterationDialogData;

    /**
     * SETTERS
     */
    public void popDialogStack() {
        if (this.dialogStack.empty()) {
            this.topOfDialogStack.setValue(null);
        } else {
            this.topOfDialogStack.setValue(this.dialogStack.pop());
        }
    }

    /**
     * GETTERS
     */
    public LiveData<DialogData> getTopOfDialogStack() {
        return this.topOfDialogStack;
    }

    /**
     * FUNCTIONS
     */
    public void pushDialogStack(DialogData dialogData) {
        this.dialogStack.push(dialogData);
        if (!nonNull(this.topOfDialogStack.getValue())) {
            popDialogStack();
        }
    }
}