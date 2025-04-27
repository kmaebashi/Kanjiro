package com.kmaebashi.kanjiro.controller.data;

public class DeleteAnswerResult {
    public boolean deleted;
    public String warningMessage;

    public DeleteAnswerResult(boolean deleted, String message) {
        this.deleted = deleted;
        this.warningMessage = message;
    }
}
