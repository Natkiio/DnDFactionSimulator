package org.example.dndfactionsimulator.model;

public class Faction {
    // Database ID
    private int id;

    // Basic Info
    private String name;
    private FactionType type;
    private Alignment alignment;

    // Resources
    private int gold;
    private int troops;
    private int magic;
    private int influence;

    // State
    private boolean isActive;

    // Constructors
    public Faction() {
        this.isActive = true;
    }

    public Faction(String name, FactionType type, Alignment alignment) {
        this.name = name;
        this.type = type;
        this.alignment = alignment;
        this.isActive = true;

        // Starting resources
        this.gold = 100;
        this.troops = 50;
        this.magic = 10;
        this.influence = 20;
    }

    // Calculated strength
    public int getStrength() {
        return troops + (gold / 10) + (magic * 2) + influence;
    }

    // Check if faction is alive
    public boolean isAlive() {
        return isActive && getStrength() > 0;
    }

    // Modify resources safely
    public void addGold(int amount) {
        this.gold = Math.max(0, this.gold + amount);
    }

    public void addTroops(int amount) {
        this.troops = Math.max(0, this.troops + amount);
    }

    public void addMagic(int amount) {
        this.magic = Math.max(0, this.magic + amount);
    }

    public void addInfluence(int amount) {
        this.influence = Math.max(0, this.influence + amount);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FactionType getType() {
        return type;
    }

    public void setType(FactionType type) {
        this.type = type;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public int getTroops() {
        return troops;
    }

    public void setTroops(int troops) {
        this.troops = Math.max(0, troops);
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = Math.max(0, magic);
    }

    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        this.influence = Math.max(0, influence);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Strength: %d [Gold: %d, Troops: %d, Magic: %d, Influence: %d]",
                name, type, getStrength(), gold, troops, magic, influence);
    }
}