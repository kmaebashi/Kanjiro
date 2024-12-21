package com.kmaebashi.kanjiro.controller.data;

public class PostEventInfoResult {
    public String eventId;
    public boolean registered;
    public String warningMessage;
    public String updatedAt;

    public PostEventInfoResult(String eventId, boolean registered,
                               String warningMessage, String updatedAt) {
        this.eventId = eventId;
        this.registered = registered;
        this.warningMessage = warningMessage;
        this.updatedAt = updatedAt;
    }
}
