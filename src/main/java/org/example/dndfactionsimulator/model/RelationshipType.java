package org.example.dndfactionsimulator.model;

public enum RelationshipType {
    ALLIED("Allied"),
    FRIENDLY("Friendly"),
    NEUTRAL("Neutral"),
    UNFRIENDLY("Unfriendly"),
    HOSTILE("Hostile"),
    AT_WAR("At War");

    private final String displayName;

    RelationshipType(String displayName) {
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