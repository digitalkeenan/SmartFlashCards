package com.example.smartflashcards.stackDetails;

import android.content.Context;

import com.example.smartflashcards.R;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

import java.util.Locale;

public class StackDetails {

    private String databaseVersion;
    private String description;
    private String questionLabel;
    private String questionPrefix;
    private String questionPostfix;
    private String answerLabel;
    private String jeopardyPrefix;
    private String jeopardyPostfix;
    private Locale questionLocale;
    private Locale answerLocale;

    public StackDetails(Context context) {
        this.databaseVersion = context.getString(R.string.database_version);
        this.description = "";
        this.questionLocale = Locale.getDefault();
        this.questionLabel = context.getString(R.string.stack_question_default);
        this.questionPrefix = context.getString(R.string.stack_question_prefix);
        this.questionPostfix = context.getString(R.string.stack_question_postfix);
        this.answerLocale = Locale.getDefault();
        this.answerLabel = context.getString(R.string.stack_answer_default);
        this.jeopardyPrefix = context.getString(R.string.stack_jeopardy_pretfix);
        this.jeopardyPostfix = context.getString(R.string.stack_jeopardy_postfix);
    }

    public StackDetails(MyFileInputStream inputStream) {
        this.databaseVersion = inputStream.readString();
        this.description = inputStream.readString();
        if (this.databaseVersion.equals("0.01")) {
            this.questionLocale = Locale.ENGLISH;
        } else {
            this.questionLocale = new Locale(inputStream.readString());
        }
        this.questionLabel = inputStream.readString();
        this.questionPrefix = inputStream.readString();
        this.questionPostfix = inputStream.readString();
        if (this.databaseVersion.equals("0.01")) {
            this.answerLocale = new Locale("es");
        } else {
            this.answerLocale = new Locale(inputStream.readString());
        }
        this.answerLabel = inputStream.readString();
        this.jeopardyPrefix = inputStream.readString();
        this.jeopardyPostfix = inputStream.readString();
    }

    public void writeFile(String databaseVersion, MyFileOutputStream outputStream) {
        outputStream.writeString(databaseVersion);
        outputStream.writeString(this.description);
        outputStream.writeString(this.questionLocale.getLanguage());
        outputStream.writeString(this.questionLabel);
        outputStream.writeString(this.questionPrefix);
        outputStream.writeString(this.questionPostfix);
        outputStream.writeString(this.answerLocale.getLanguage());
        outputStream.writeString(this.answerLabel);
        outputStream.writeString(this.jeopardyPrefix);
        outputStream.writeString(this.jeopardyPostfix);
    }


    public void setDescription(String string) {
        this.description = string;
    }

    public void setQuestionLabel(String string) {
        this.questionLabel = string;
    }

    public void setQuestionPrefix(String string) {
        this.questionPrefix = string;
    }

    public void setQuestionPostfix(String string) {
        this.questionPostfix = string;
    }

    public void setAnswerLabel(String string) {
        this.answerLabel = string;
    }

    public void setJeopardyPrefix(String string) {
        this.jeopardyPrefix = string;
    }

    public void setJeopardyPostfix(String string) {
        this.jeopardyPostfix = string;
    }

    public void setQuestionLocale(Locale locale) {
        this.questionLocale = locale;
    }

    public void setAnswerLocale(Locale locale) {
        this.answerLocale = locale;
    }


    public String getDatabaseVersion() {
        return this.databaseVersion;
    }

    public String getDescription() {
        return this.description;
    }

    public String getQuestionLabel() {
        return this.questionLabel;
    }

    public String getQuestionPrefix() {
        return this.questionPrefix;
    }

    public String getQuestionPostfix() {
        return this.questionPostfix;
    }

    public String getAnswerLabel() {
        return this.answerLabel;
    }

    public String getJeopardyPrefix() {
        return this.jeopardyPrefix;
    }

    public String getJeopardyPostfix() {
        return this.jeopardyPostfix;
    }

    public Locale getQuestionLocale() {
        return this.questionLocale;
    }

    public Locale getAnswerLocale() {
        return this.answerLocale;
    }
}
