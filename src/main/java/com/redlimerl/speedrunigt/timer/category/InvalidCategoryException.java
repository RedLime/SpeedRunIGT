package com.redlimerl.speedrunigt.timer.category;

public class InvalidCategoryException extends Exception {
    private final Reason reason;
    private final String details;

    public Reason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    @SuppressWarnings("unused")
    public enum Reason {
        FAILED_JSON_PARSE,
        INVALID_JSON_DATA,
        UNKNOWN_EVENT_TYPE,
        UNSUPPORTED_EVENT_TYPE,
        UNSUPPORTED_CATEGORY_VERSION,
        DUPLICATED_CATEGORY_ID
    }

    public InvalidCategoryException(Reason reason, String details) {
        this.reason = reason;
        this.details = details;
    }
}
