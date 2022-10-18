package com.example.smartflashcards.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.smartflashcards.R;

public class ContinueDialogFragment extends DialogStackFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Pass null as the parent view because it's going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_continue_dialog, null);

        TextView textView = view.findViewById(R.id.message);
        textView.setText(super.dialogData.getMessage());

        builder.setPositiveButton(R.string.continueButton, null);
        builder.setView(view);
        return builder.create();
    }
}