package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.databinding.FragmentFirstBinding;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.recyclers.stackView.AlphabeticalStackViewFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private CardStackViewModel cardStackViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem exportSelection = menu.add(R.string.action_export_selection);
        MenuItem importCards = menu.add(R.string.action_import_cards);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.binding = FragmentFirstBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.cardStackViewModel = new ViewModelProvider(requireActivity()).get(CardStackViewModel.class);
        this.cardStackViewModel.getStackName().observe(getViewLifecycleOwner(), stackName -> {
            Button stackSelectionButton = (Button)view.findViewById(R.id.button_select_stack);
            stackSelectionButton.setText(stackName);
        });

        this.binding.buttonViewCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nonNull(cardStackViewModel.getStackName().getValue())) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_stackViewFragment);
                } else {
                    Context context = getContext();
                    Toast toast = Toast.makeText(context, "You must select a flashcard stack to review it's cards.", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

        this.binding.buttonSelectStack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_stackSelectionFragment);
            }
        });

        this.binding.buttonQuizme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nonNull(cardStackViewModel.getStackName().getValue())) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_quizmeFragment);
                } else {
                    Context context = getContext();
                    Toast toast = Toast.makeText(context, "You must select a flashcard stack before beginning quiz.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

}