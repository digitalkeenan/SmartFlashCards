package com.example.smartflashcards.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartflashcards.CardStackViewModel;
import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.R;

public class RemoveChangeCancelDialogFragment extends DialogStackFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        CardStackViewModel cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_remove_change_cancel_dialog, null);

        TextView textView = view.findViewById(R.id.message);
        textView.setText(super.dialogData.getMessage());

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.removeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch (dialogData.getAction()) {
                            case deleteOrChangeQuestionCard:
                                cardStackViewModel.deleteQuestion(dialogData.getFlashCard().getCardText());
                                break;
                        }
                    }
                })
                .setNeutralButton(R.string.changeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (dialogData.getAction()) {
                            case deleteOrChangeQuestionCard:
                                cardStackViewModel.createModifyAnswerDialog(
                                        dialogData.getDataString(),
                                        (QuestionCard) dialogData.getFlashCard());
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RemoveChangeCancelDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}