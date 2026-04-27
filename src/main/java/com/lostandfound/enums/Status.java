package com.lostandfound.enums;

public enum Status {
    FOUND("Found - Awaiting Claim"),
    CLAIMED("Claimed"),
    LOST("Lost - Searching"),
    MATCHED("Matched - Pending Verification"),
    EXPIRED("Expired");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
