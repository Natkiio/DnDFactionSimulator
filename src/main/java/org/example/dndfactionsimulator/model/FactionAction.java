package org.example.dndfactionsimulator.model;

public enum FactionAction {
    GATHER_RESOURCES("Gather Resources", "Collect gold and materials"),
    RECRUIT_TROOPS("Recruit Troops", "Train new soldiers"),
    EXPAND_INFLUENCE("Expand Influence", "Spread political reach"),
    STUDY_MAGIC("Study Magic", "Research arcane knowledge"),
    ATTACK("Attack Enemy", "Launch military assault"),
    RAID("Raid", "Quick strike for resources"),
    FORM_ALLIANCE("Form Alliance", "Establish diplomatic ties"),
    BREAK_ALLIANCE("Break Alliance", "End diplomatic agreement"),
    TRADE("Trade", "Exchange resources with allies"),
    FORTIFY("Fortify", "Strengthen defenses"),
    SPY("Spy", "Gather intelligence"),
    SABOTAGE("Sabotage", "Undermine enemy operations"),
    INTERNAL_DECAY("Internal Decay", "Suffer from corruption/rebellion");

    private final String displayName;
    private final String description;

    FactionAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}