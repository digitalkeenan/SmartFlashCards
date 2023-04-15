package com.example.smartflashcards.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.R;
import com.example.smartflashcards.stackDetails.StackDetailsViewModel;

import java.io.File;

public class NewStackDialogFragment extends DialogStackFragment {

    private EditText editStackName;
    CardStackViewModel cardStackViewModel;
    StackDetailsViewModel stackDetailsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_new_stack_dialog, null);

        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        this.stackDetailsViewModel = new ViewModelProvider(requireActivity()).get(StackDetailsViewModel.class);

        this.editStackName = view.findViewById(R.id.textInputNewStack);
        this.editStackName.requestFocus();
        this.editStackName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    handled = true;
                }
                return handled;
            }
        });

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.saveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        submit();
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewStackDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void submit() {
        //TODO: force all-caps and check for invalid characters,etc.
        // note: the field is set to all caps, but the user can still hit the shift key and break that
        File directory = new File(getActivity().getFilesDir(), getString(R.string.stack_directory));
        File stack = new File(directory, this.editStackName.getText().toString().trim());
        if (stack.exists()) {
            DialogData newDialogData = new DialogData(DialogData.Type.CONTINUE, DialogData.Action.noAction);
            newDialogData.setMessage(this.editStackName.getText().toString() + " already exists");
            this.cardStackViewModel.setDialogData(newDialogData);
        } else {
            stack.mkdir();
        }
        if (stack.exists()) {
            // If it actually created, update the details view model
            // (don't select in cardStackViewModel until exiting stackSelectionFragment)
            this.stackDetailsViewModel.selectStack(this.editStackName.getText().toString());
        }
        dismiss();
    }
}