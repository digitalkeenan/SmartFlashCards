package com.example.smartflashcards;

import static java.util.Objects.nonNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.smartflashcards.cards.QuestionCard;
import com.example.smartflashcards.databinding.ActivityMainBinding;
import com.example.smartflashcards.dialogs.CardBuilderDialogFragment;
import com.example.smartflashcards.dialogs.ContinueDialogFragment;
import com.example.smartflashcards.dialogs.ContinueOrCancelDialogFragment;
import com.example.smartflashcards.dialogs.DialogStackFragment;
import com.example.smartflashcards.dialogs.DialogViewModel;
import com.example.smartflashcards.dialogs.NewStackDialogFragment;
import com.example.smartflashcards.dialogs.ProposeCardReviewDialogFragment;
import com.example.smartflashcards.keenanClasses.MyAutoCloseInputStream;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;
import com.example.smartflashcards.stackDetails.StackDetails;
import com.example.smartflashcards.stackDetails.StackDetailsViewModel;

import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public File stackDir;
    public File detailsStack; // directory that stack detail view/edit process is using
    public File activeStack;  // directory of the stack that is actively being built and/or quized

    private CardStackViewModel cardStackViewModel;
    private StackDetailsViewModel stackDetailsViewModel;
    private DialogViewModel dialogViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();

        //TODO: move to where permission is needed
        // add check for self-permission
        // add check for rational required
        // gracefully handle permission-denied
        //Manifest.permission.REQU

        this.stackDir = new File(context.getFilesDir(), getString(R.string.stack_directory));
        if (!this.stackDir.exists()) {
            this.stackDir.mkdir();
        }

        this.stackDetailsViewModel = new ViewModelProvider(this).get(StackDetailsViewModel.class);
        this.cardStackViewModel = new ViewModelProvider(this).get(CardStackViewModel.class);
        this.dialogViewModel = new ViewModelProvider(this).get(DialogViewModel.class);

        /**
         * observe STACK DETAILS UPDATED
         */
        this.stackDetailsViewModel.getStackDetailsUpdated().observe(this, stackDetailsUpdated -> {
            if (nonNull(stackDetailsUpdated)) {
                File stack = new File(this.stackDir, this.stackDetailsViewModel.getStackName().getValue());
                writeDetailsFile(stack, this.stackDetailsViewModel.getStackDetails().getValue());
                // if details are updated in stackDetailsViewModel
                // and it's the same stack as in cardStackViewModel, update...
                if (nonNull(this.cardStackViewModel.getStackName().getValue())
                        && stackDetailsUpdated.equals(this.cardStackViewModel.getStackName().getValue())){
                    String currentQuestionLanguage =
                            this.cardStackViewModel.getStackDetails().getValue().getQuestionLocale().getLanguage();
                    String currentAnswerLanguage =
                            this.cardStackViewModel.getStackDetails().getValue().getAnswerLocale().getLanguage();
                    String newQuestionLanguage =
                            this.stackDetailsViewModel.getStackDetails().getValue().getQuestionLocale().getLanguage();
                    String newAnswerLanguage =
                            this.stackDetailsViewModel.getStackDetails().getValue().getAnswerLocale().getLanguage();
                    boolean languageChanged = !(newQuestionLanguage.equals(currentQuestionLanguage)
                                            && newAnswerLanguage.equals(currentAnswerLanguage));

                    this.cardStackViewModel.setStackDetails(this.stackDetailsViewModel.getStackDetails().getValue());

                    // if a language changed, must reload to re-sort alphabetical stacks
                    if (languageChanged) {
                        MyFileInputStream alphaInputStream = openInputStream(this.activeStack, getString(R.string.answer_card_file));
                        MyFileInputStream quizInputStream = openInputStream(this.activeStack, getString(R.string.quiz_card_file));
                        if (nonNull(alphaInputStream) && nonNull(quizInputStream)) {
                            //TODO: when adding file-count, read and write each file,
                            // watching for any cards that need to switch files
                            this.cardStackViewModel.loadFlashcardStack(
                                    this.cardStackViewModel.getStackDetails().getValue().getDatabaseVersion(),
                                    alphaInputStream,
                                    quizInputStream);
                            closeInputStream(alphaInputStream);
                            closeInputStream(quizInputStream);
                        }
                    }
                }
            }
        });

        /**
         * observe STACK SELECTED
         */
        this.cardStackViewModel.getStackName().observe(this, stackName -> {
            if (nonNull(stackName)) {
                String currentVersion = getString(R.string.database_version);
                String readVersion = null;
                this.activeStack = new File(this.stackDir, stackName);

                if (this.activeStack.exists()) {
                    //
                    // LOAD STACK DETAILS
                    //
                    String fileName = getString(R.string.stack_details_file);
                    MyFileInputStream inputStream = openInputStream(this.activeStack, fileName);
                    if (nonNull(inputStream)) {
                        this.cardStackViewModel.loadStackDetails(inputStream);
                        closeInputStream(inputStream);
                        readVersion = this.cardStackViewModel.getStackDetails().getValue().getDatabaseVersion();
                        if (!readVersion.equals(currentVersion)) {
                            MyFileOutputStream outputStream = openOutputStream(this.activeStack, fileName);
                            if (nonNull(outputStream)) {
                                this.cardStackViewModel.getStackDetails().getValue().
                                        writeFile(currentVersion, outputStream);
                                closeOutputStream(this.activeStack, outputStream, fileName);
                            }
                        }
                    } else {
                        //if not loading selected stack, create and save default stack details
                        this.cardStackViewModel.newStackDetails(context);
                        writeDetailsFile(this.activeStack, this.cardStackViewModel.getStackDetails().getValue());
                    }
                    //
                    // LOAD STACKS
                    //
                    MyFileInputStream alphaInputStream = openInputStream(this.activeStack, getString(R.string.answer_card_file));
                    MyFileInputStream quizInputStream = openInputStream(this.activeStack, getString(R.string.quiz_card_file));
                    if (nonNull(readVersion) && nonNull(alphaInputStream) && nonNull(quizInputStream)) {
                        //TODO: add file-count and only read first file initially here
                        // but if databaseVersion changed, read and write each one
                        this.cardStackViewModel.loadFlashcardStack(
                                readVersion, alphaInputStream, quizInputStream);
                        closeInputStream(alphaInputStream);
                        closeInputStream(quizInputStream);
                        if (!readVersion.equals(currentVersion)) {
                            writeAlphaFile();
                            writeQuizFile();
                        }
                    } else {
                        //if not loading selected stack, need to clear any prior stack's data
                        this.cardStackViewModel.newFlashcardStack();
                    }
                }
            }
        });

        /**
         * observe ALPHABETICAL STACKS UPDATED
         */
        this.cardStackViewModel.getAlphaStackChanged().observe(this, updated -> {
            if (updated) {
                writeAlphaFile();
            }
        });

        /**
         * observe QUIZ STACK UPDATED
         */
        this.cardStackViewModel.getQuizStackChanged().observe(this, updated -> {
            if (updated) {
                writeQuizFile();
            }
        });

        /**
         * OBSERVE DIALOG DATA and push to dialogViewModel
         */
        this.cardStackViewModel.getDialogData().observe(this, dialogData -> {
            if (nonNull(dialogData)) {
                this.dialogViewModel.pushDialogStack(dialogData);
            }
        });

        /**
         * OBSERVE DIALOG DATA STACK
         */
        dialogViewModel.getTopOfDialogStack().observe(this, dialogData -> {
            if (nonNull(dialogData)) {
                DialogStackFragment dialogFragment = null;
                switch (dialogData.getType()) {
                    case CONTINUE:
                        dialogFragment = new ContinueDialogFragment();
                        break;
                    case CONTINUE_OR_CANCEL:
                        dialogFragment = new ContinueOrCancelDialogFragment();
                        break;
                    case CREATE_NEW_STACK:
                        dialogFragment = new NewStackDialogFragment();
                        break;
                    case CREATE_NEW_FLASHCARD:
                    case EDIT_CARD_TEXT:
                    case ENTER_NEW_ANSWER:
                    case ENTER_NEW_QUESTION:
                        dialogFragment = new CardBuilderDialogFragment();
                        break;
                    case RECOMMEND_REVIEW:
                        dialogFragment = new ProposeCardReviewDialogFragment();
                        break;
                }
                if (nonNull(dialogFragment)) {
                    dialogFragment.show(getSupportFragmentManager(), dialogData.getType().toString());
                }
            }
        });

        /**
         * FINISH ON-CREATE
         */
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        //TODO: if this works, add getter in view model and somehow kill the new navcontroller
        // else, try navigating with old navcontroller (didn't help doing this here, but it did recognize the correct state of the old navcontroller - so it is still active ):
        // try setviewnavcontroller
        // try navController.saveState and navController.restoreState (didn't work ):
        /*if (nonNull(this.cardStackViewModel.navController)) {
            navController = this.cardStackViewModel.navController;
            navController.navigate(R.id.action_stackSelectionFragment_to_stackDetailsFragment);
        } else*/ {
            this.cardStackViewModel.navController = navController;
        }

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (nonNull(savedInstanceState)) {
            //TODO: restore what must be restored to preserve operation during orientation change, etc.
            navController.restoreState(savedInstanceState.getBundle("navController"));
        }
    }

    private MyFileInputStream openInputStream(File stack, String fileName) {
        File file = new File(stack, fileName);
        MyFileInputStream stream = null;
        try {
            stream = new MyFileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stream;
    }

    private void closeInputStream(MyFileInputStream stream) {
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MyFileOutputStream openOutputStream(File stack, String fileName) {
        MyFileOutputStream stream = null;
        File file = new File(stack, fileName);
        File backupfile = new File(stack, fileName + "_backup");
        try {
            if (backupfile.exists()) {
                backupfile.delete();
            }
            if (file.exists()) {
                file.renameTo(backupfile);
            }
            file.createNewFile();
            stream = new MyFileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            //if something went wrong, try to restore last file
            try {
                if (file.exists()) {
                    file.delete();
                }
                if (backupfile.exists()) {
                    backupfile.renameTo(file);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return stream;
    }

    private boolean closeOutputStream(File stack, MyFileOutputStream stream, String fileName) {
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File(stack, fileName);
        File backupfile = new File(stack, fileName + "_backup");
        if (file.exists() && (file.length() > 0)) {
            if (backupfile.exists()) {
                backupfile.delete();
            }
            return true;
        }
        // if file exists but is empty, restore backup file
        if (file.exists()) {
            file.delete();
        }
        if (backupfile.exists()) {
            backupfile.renameTo(file);
        }
        return false;
    }

    private void writeDetailsFile(File stack, StackDetails stackDetails) {
        MyFileOutputStream outputStream = openOutputStream(stack, getString(R.string.stack_details_file));
        if (nonNull(outputStream)) {
            stackDetails.writeFile(getString(R.string.database_version), outputStream);
            closeOutputStream(stack, outputStream, getString(R.string.stack_details_file));
        }
    }

    private void writeAlphaFile() {
        String fileName = getString(R.string.answer_card_file);
        MyFileOutputStream outputStream = openOutputStream(this.activeStack, fileName);
        if (nonNull(outputStream)) {
            outputStream.writeString(getString(R.string.database_version));
            this.cardStackViewModel.saveAlphabeticalStack(outputStream);
            closeOutputStream(this.activeStack, outputStream, fileName);
        }
    }

    private void writeQuizFile() {
        String fileName = getString(R.string.quiz_card_file);
        MyFileOutputStream outputStream = openOutputStream(this.activeStack, fileName);
        if (nonNull(outputStream)) {
            this.cardStackViewModel.saveQuizStack(outputStream);
            closeOutputStream(this.activeStack, outputStream, fileName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TODO: save what must be saved to preserve operation during orientation change, etc.
        outState.putBundle("navController", this.cardStackViewModel.navController.saveState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = getApplicationContext();

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //TODO: replace this toast with something to implement settings option
            // perhaps keep them in a user file
            // - change/create user (someday add login if becomes an online tool with web interface)
            // - colors
            // - rotation lock
            // - language selection
            Toast toast = Toast.makeText(context, "main menu settings option selected", Toast.LENGTH_LONG);
            toast.show();

            return true;
        }
        CharSequence title = item.getTitle();
        if (nonNull(title)) { //check for null because back button also calls this and has no title
            /**
             * OPTION EXPORT CARDS
             */
            if (title.equals(getString(R.string.action_export_cards))) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, 10);
                return true;
            }
            /**
             * OPTION IMPORT CARDS
             */
            if (title.equals(getString(R.string.action_import_cards))) {
                if (nonNull(cardStackViewModel.getStackName().getValue())) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, 20);
                } else {
                    Toast toast = Toast.makeText(context, "You must select a flashcard stack into which to import.", Toast.LENGTH_LONG);
                    toast.show();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 10) {
                /*
                EXPORT CARDS
                 */
                if (resultData != null) {
                    Uri uri = resultData.getData();

                    File directory = new File(getApplication().getFilesDir(), getString(R.string.stack_directory));
                    File stackDir = new File(directory, this.cardStackViewModel.getStackName().getValue());
                    File internalFile = new File (stackDir, "answer_cards");
                    FileChannel inChannel = null;
                    FileChannel outChannel = null;

                    try {
                        inChannel = new FileInputStream(internalFile).getChannel();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        ParcelFileDescriptor pfd = this.getContentResolver().
                                openFileDescriptor(uri, "w");

                        outChannel = new MyFileOutputStream(pfd.getFileDescriptor()).getChannel();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    try {
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                        if (inChannel != null)
                            inChannel.close();
                        if (outChannel != null)
                            outChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == 20) {
                /*
                IMPORT CARDS
                 */
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    //TODO: add something to the file to check if it is a valid file (like a special starting string)

                    //switched to auto-close input stream to fix Bad File Descriptor error
                    // which randomly would stop the read in the middle
                    MyAutoCloseInputStream alphaInputStream = null;
                    try {
                        ParcelFileDescriptor pfd = this.getContentResolver().
                                        openFileDescriptor(uri, "r");
                        //alphaInputStream = new MyFileInputStream(pfd.getFileDescriptor());
                        alphaInputStream = new MyAutoCloseInputStream(pfd);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    alphaInputStream.readString(); // version code
                    alphaInputStream.readInt(); // next ID
                    int numberOfCards = alphaInputStream.readInt();
                    for (int cardIndex=0; cardIndex < numberOfCards; cardIndex++) {
                        alphaInputStream.readInt(); // ID number
                        QuestionCard questionCard = new QuestionCard(alphaInputStream);
                        questionCard.getAnswers().forEach((key, answer) -> {
                            this.cardStackViewModel.addQuestionCard(questionCard.getCardText(),
                                    (String) answer, null, 10, false);
                        });
                    }

                    try {
                        alphaInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}