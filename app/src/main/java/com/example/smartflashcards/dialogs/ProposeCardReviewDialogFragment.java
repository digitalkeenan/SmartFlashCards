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
import com.example.smartflashcards.R;

public class ProposeCardReviewDialogFragment extends DialogStackFragment {

    private CardStackViewModel cardStackViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_propose_card_review_dialog, null);

        TextView textView = view.findViewById(R.id.message);
        textView.setText(dialogData.getMessage());

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.reviewCardButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        cardStackViewModel.setForceCardReview(dialogData.getFlashCard());
                    }
                })
                .setNegativeButton(R.string.continueButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ProposeCardReviewDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}