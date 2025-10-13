package org.example.dndfactionsimulator.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorldEvent {
    private int id;
    private int turnNumber;
    private int factionId;
    private FactionAction action;
    private String description;
    private LocalDateTime timestamp;
    private Integer targetFactionId;  // Optional - if action involves another faction

    public WorldEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public WorldEvent(int turnNumber, int factionId, FactionAction action, String description) {
        this.turnNumber = turnNumber;
        this.factionId = factionId;
        this.action = action;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getFactionId() {
        return factionId;
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public FactionAction getAction() {
        return action;
    }

    public void setAction(FactionAction action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getTargetFactionId() {
        return targetFactionId;
    }

    public void setTargetFactionId(Integer targetFactionId) {
        this.targetFactionId = targetFactionId;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[Turn %d] %s - %s", turnNumber, action.getDisplayName(), description);
    }
}