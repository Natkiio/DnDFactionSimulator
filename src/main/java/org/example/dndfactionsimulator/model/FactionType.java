package org.example.dndfactionsimulator.model;

public enum FactionType {
    KINGDOM("Kingdom"),
    GUILD("Guild"),
    CLAN("Clan"),
    CULT("Cult"),
    MERCENARY("Mercenary Company"),
    MONSTER_HORDE("Monster Horde"),
    TRADING_COMPANY("Trading Company"),
    RELIGIOUS_ORDER("Religious Order");

    private final String displayName;

    FactionType(String displayName) {
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