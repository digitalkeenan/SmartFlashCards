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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContinueOrCancelDialogFragment extends DialogStackFragment {
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
        View view = inflater.inflate(R.layout.fragment_continue_or_cancel_dialog, null);

        TextView textView = view.findViewById(R.id.message);
        textView.setText(super.dialogData.getMessage());

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.continueButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch (dialogData.getAction()) {
                            case deleteAnswerFromQuestionCard:
                                cardStackViewModel.deleteAnswerFromQuestionCard(
                                        dialogData.getDataString(),
                                        (QuestionCard) dialogData.getFlashCard(), false);
                                break;
                            case deleteQuestion:
                                cardStackViewModel.deleteQuestion(dialogData.getDataString());
                                break;
                            case deleteQuestionFromAnswerCardReview:
                                cardStackViewModel.deleteQuestionFromAnswerCardReview(
                                        dialogData.getFlashCard().getCardText(), // question
                                        dialogData.getDataString() //answer
                                );
                                break;
                            case deleteStack:
                                File directory = new File(getActivity().getFilesDir(), getString(R.string.stack_directory));
                                File stack = new File(directory, dialogData.getDataString());
                                if (stack.exists()) {
                                    // first remove contents
                                    // TODO: to make this a useful/generic tool,
                                    //  it should use a directory deleting routine that checks for subdirectories and recursively calls itself to remove them
                                    List<String> filesList = new ArrayList(Arrays.asList(stack.list())); //TODO: is there a way to get the files directly instead of through strings?
                                    filesList.forEach(fileString -> {
                                        File file = new File(stack, fileString);
                                        file.delete();
                                    });
                                    stack.delete();
                                    cardStackViewModel.setDeletedStack(dialogData.getDataString());
                                }
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ContinueOrCancelDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}