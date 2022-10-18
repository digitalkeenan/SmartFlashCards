package com.example.smartflashcards.stackDetails;

import android.content.Context;

import com.example.smartflashcards.R;
import com.example.smartflashcards.keenanClasses.MyFileInputStream;
import com.example.smartflashcards.keenanClasses.MyFileOutputStream;

public class StackDetails {

    private String databaseVersion;
    private String description;
    private String questionLabel;
    private String questionPrefix;
    private String questionPostfix;
    private String answerLabel;
    private String jeopardyPrefix;
    private String jeopardyPostfix;

    public StackDetails(Context context) {
        this.databaseVersion = context.getString(R.string.database_version);
        this.description = "";
        this.questionLabel = context.getString(R.string.stack_question_default);
        this.questionPrefix = context.getString(R.string.stack_question_prefix);
        this.questionPostfix = context.getString(R.string.stack_question_postfix);
        this.answerLabel = context.getString(R.string.stack_answer_default);
        this.jeopardyPrefix = context.getString(R.string.stack_jeopardy_pretfix);
        this.jeopardyPostfix = context.getString(R.string.stack_jeopardy_postfix);
    }

    public StackDetails(MyFileInputStream inputStream) {
        this.databaseVersion = inputStream.readString();
        this.description = inputStream.readString();
        this.questionLabel = inputStream.readString();
        this.questionPrefix = inputStream.readString();
        this.questionPostfix = inputStream.readString();
        this.answerLabel = inputStream.readString();
        this.jeopardyPrefix = inputStream.readString();
        this.jeopardyPostfix = inputStream.readString();
    }

    public void writeFile(String databaseVersion, MyFileOutputStream outputStream) {
        outputStream.writeString(databaseVersion);
        outputStream.writeString(this.description);
        outputStream.writeString(this.questionLabel);
        outputStream.writeString(this.questionPrefix);
        outputStream.writeString(this.questionPostfix);
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
        return questionPostfix;
    }

    public String getAnswerLabel() {
        return answerLabel;
    }

    public String getJeopardyPrefix() {
        return jeopardyPrefix;
    }

    public String getJeopardyPostfix() {
        return jeopardyPostfix;
    }
}
