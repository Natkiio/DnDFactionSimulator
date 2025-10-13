package org.example.dndfactionsimulator.model;

public class Relationship {
    private int id;
    private int faction1Id;  // First faction
    private int faction2Id;  // Second faction
    private RelationshipType type;
    private int strength;    // -100 (war) to +100 (strong alliance)

    public Relationship() {}

    public Relationship(int faction1Id, int faction2Id, RelationshipType type, int strength) {
        this.faction1Id = faction1Id;
        this.faction2Id = faction2Id;
        this.type = type;
        this.strength = strength;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFaction1Id() {
        return faction1Id;
    }

    public void setFaction1Id(int faction1Id) {
        this.faction1Id = faction1Id;
    }

    public int getFaction2Id() {
        return faction2Id;
    }

    public void setFaction2Id(int faction2Id) {
        this.faction2Id = faction2Id;
    }

    public RelationshipType getType() {
        return type;
    }

    public void setType(RelationshipType type) {
        this.type = type;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        // Keep between -100 and +100
        this.strength = Math.max(-100, Math.min(100, strength));
    }

    public void adjustStrength(int change) {
        setStrength(this.strength + change);
    }

    @Override
    public String toString() {
        return String.format("Relationship: Faction %d <-> Faction %d [%s: %d]",
                faction1Id, faction2Id, type, strength);
    }
}