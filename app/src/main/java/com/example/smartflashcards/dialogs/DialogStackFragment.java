package com.example.smartflashcards.dialogs;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogStackFragment extends DialogFragment {

    DialogViewModel viewModel;
    DialogData dialogData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.viewModel = new ViewModelProvider(requireActivity()).get(DialogViewModel.class);
        this.dialogData = viewModel.getTopOfDialogStack().getValue();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.viewModel.popDialogStack();
    }
}