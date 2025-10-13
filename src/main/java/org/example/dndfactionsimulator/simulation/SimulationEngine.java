package org.example.dndfactionsimulator.simulation;

import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class SimulationEngine {

    private DatabaseManager db;
    private Random random;

    public SimulationEngine(DatabaseManager db) {
        this.db = db;
        this.random = new Random();
    }

    /**
     * Main method to run a complete turn simulation
     * Returns a list of events that occurred this turn
     */
    public List<WorldEvent> runTurn() {
        List<WorldEvent> turnEvents = new ArrayList<>();

        // Get current turn and all active factions
        int currentTurn = db.getCurrentTurn();
        List<Faction> activeFactions = db.getActiveFactions();

        if (activeFactions.isEmpty()) {
            System.out.println("⚠️ No active factions to simulate");
            return turnEvents;
        }

        System.out.println("\n🎲 === SIMULATING TURN " + currentTurn + " ===");
        System.out.println("Active factions: " + activeFactions.size());

        // Each faction takes one action
        for (Faction faction : activeFactions) {
            WorldEvent event = processFactionTurn(faction, activeFactions, currentTurn);
            if (event != null) {
                db.addWorldEvent(event);
                turnEvents.add(event);
            }
        }

        // Apply decay/random events (10% chance per faction)
        for (Faction faction : activeFactions) {
            if (random.nextDouble() < 0.1) {
                WorldEvent decayEvent = applyRandomEvent(faction, currentTurn);
                if (decayEvent != null) {
                    db.addWorldEvent(decayEvent);
                    turnEvents.add(decayEvent);
                }
            }
        }

        // Advance the turn counter
        db.advanceTurn();

        System.out.println("✅ Turn " + currentTurn + " complete. " + turnEvents.size() + " events occurred.\n");

        return turnEvents;
    }

    /**
     * Process a single faction's turn
     */
    private WorldEvent processFactionTurn(Faction faction, List<Faction> allFactions, int turn) {
        // Choose an action based on faction state
        FactionAction action = chooseAction(faction, allFactions);

        // Execute the action and create event
        return executeAction(faction, action, allFactions, turn);
    }

    /**
     * AI logic to choose what action a faction should take
     */
    private FactionAction chooseAction(Faction faction, List<Faction> allFactions) {
        int strength = faction.getStrength();
        int averageStrength = (int) allFactions.stream()
                .mapToInt(Faction::getStrength)
                .average()
                .orElse(100);

        boolean isStrong = strength > averageStrength * 1.2;
        boolean isWeak = strength < averageStrength * 0.7;
        boolean lowResources = faction.getGold() < 50;

        // Build weighted action choices
        Map<FactionAction, Integer> weights = new HashMap<>();

        if (lowResources) {
            weights.put(FactionAction.GATHER_RESOURCES, 40);
            weights.put(FactionAction.RAID, 30);
        } else {
            weights.put(FactionAction.GATHER_RESOURCES, 15);
            weights.put(FactionAction.RAID, 10);
        }

        if (isStrong) {
            weights.put(FactionAction.ATTACK, 25);
            weights.put(FactionAction.EXPAND_INFLUENCE, 20);
        } else if (isWeak) {
            weights.put(FactionAction.FORM_ALLIANCE, 25);
            weights.put(FactionAction.FORTIFY, 20);
        }

        weights.put(FactionAction.RECRUIT_TROOPS, 15);
        weights.put(FactionAction.STUDY_MAGIC, 10);
        weights.put(FactionAction.TRADE, 10);

        // Chaotic factions more likely to attack/betray
        if (faction.getAlignment().name().contains("CHAOTIC")) {
            weights.put(FactionAction.ATTACK, weights.getOrDefault(FactionAction.ATTACK, 0) + 15);
            weights.put(FactionAction.RAID, weights.getOrDefault(FactionAction.RAID, 0) + 10);
        }

        // Lawful factions more likely to trade/ally
        if (faction.getAlignment().name().contains("LAWFUL")) {
            weights.put(FactionAction.TRADE, weights.getOrDefault(FactionAction.TRADE, 0) + 10);
            weights.put(FactionAction.FORM_ALLIANCE, weights.getOrDefault(FactionAction.FORM_ALLIANCE, 0) + 10);
        }

        // Select action based on weights
        return weightedRandomChoice(weights);
    }

    /**
     * Execute a faction's chosen action
     */
    private WorldEvent executeAction(Faction faction, FactionAction action, List<Faction> allFactions, int turn) {
        WorldEvent event = new WorldEvent(turn, faction.getId(), action, "");
        String description = faction.getName() + " ";

        switch (action) {
            case GATHER_RESOURCES:
                int goldGained = random.nextInt(30) + 20; // 20-50 gold
                faction.addGold(goldGained);
                description += "gathered resources, gaining " + goldGained + " gold.";
                break;

            case RECRUIT_TROOPS:
                if (faction.getGold() >= 30) {
                    int troopsGained = random.nextInt(15) + 10; // 10-25 troops
                    faction.addTroops(troopsGained);
                    faction.addGold(-30);
                    description += "recruited " + troopsGained + " troops for 30 gold.";
                } else {
                    description += "attempted to recruit troops but lacked funds.";
                }
                break;

            case EXPAND_INFLUENCE:
                if (faction.getGold() >= 20) {
                    int influenceGained = random.nextInt(8) + 5; // 5-12 influence
                    faction.addInfluence(influenceGained);
                    faction.addGold(-20);
                    description += "expanded their influence by " + influenceGained + ".";
                } else {
                    description += "attempted to expand influence but lacked funds.";
                }
                break;

            case STUDY_MAGIC:
                if (faction.getGold() >= 25) {
                    int magicGained = random.nextInt(5) + 3; // 3-7 magic
                    faction.addMagic(magicGained);
                    faction.addGold(-25);
                    description += "studied arcane arts, gaining " + magicGained + " magic.";
                } else {
                    description += "attempted to study magic but lacked funds.";
                }
                break;

            case ATTACK:
                Faction target = findWeakestEnemy(faction, allFactions);
                if (target != null) {
                    int damage = faction.getStrength() / 5 + random.nextInt(20);
                    target.addTroops(-damage);
                    faction.addTroops(-damage / 3); // Attacker takes casualties too
                    description += "attacked " + target.getName() + ", dealing " + damage + " casualties!";
                    event.setTargetFactionId(target.getId());
                    db.updateFaction(target);
                } else {
                    description += "prepared for battle but found no worthy opponents.";
                }
                break;

            case RAID:
                Faction raidTarget = findWeakestFaction(faction, allFactions);
                if (raidTarget != null) {
                    int stolenGold = Math.min(raidTarget.getGold() / 2, 40);
                    raidTarget.addGold(-stolenGold);
                    faction.addGold(stolenGold);
                    description += "raided " + raidTarget.getName() + ", stealing " + stolenGold + " gold!";
                    event.setTargetFactionId(raidTarget.getId());
                    db.updateFaction(raidTarget);
                } else {
                    description += "scouted for raids but found nothing of value.";
                }
                break;

            case FORTIFY:
                if (faction.getGold() >= 15) {
                    faction.addTroops(random.nextInt(10) + 5);
                    faction.addGold(-15);
                    description += "fortified their defenses.";
                } else {
                    description += "attempted to fortify but lacked funds.";
                }
                break;

            case TRADE:
                if (faction.getGold() >= 10) {
                    faction.addGold(-10);
                    faction.addInfluence(random.nextInt(5) + 3);
                    description += "engaged in trade, boosting their reputation.";
                } else {
                    description += "sought trade partners but had nothing to offer.";
                }
                break;

            case FORM_ALLIANCE:
                description += "sent diplomatic envoys seeking allies.";
                break;

            default:
                description += "contemplated their next move.";
        }

        // Update faction in database
        db.updateFaction(faction);

        event.setDescription(description);
        return event;
    }

    /**
     * Apply random negative events (decay, disasters, etc.)
     */
    private WorldEvent applyRandomEvent(Faction faction, int turn) {
        String[] disasters = {
                "suffered from internal corruption",
                "faced a minor rebellion",
                "experienced crop failure",
                "dealt with a plague outbreak",
                "weathered a harsh winter"
        };

        String disaster = disasters[random.nextInt(disasters.length)];

        // Apply penalties
        faction.addGold(-random.nextInt(20) + 10);
        faction.addTroops(-random.nextInt(10) + 5);

        db.updateFaction(faction);

        WorldEvent event = new WorldEvent(
                turn,
                faction.getId(),
                FactionAction.INTERNAL_DECAY,
                faction.getName() + " " + disaster + "."
        );

        return event;
    }

    /**
     * Find the weakest enemy faction
     */
    private Faction findWeakestEnemy(Faction attacker, List<Faction> allFactions) {
        return allFactions.stream()
                .filter(f -> f.getId() != attacker.getId())
                .filter(Faction::isActive)
                .min(Comparator.comparingInt(Faction::getStrength))
                .orElse(null);
    }

    /**
     * Find the weakest faction overall
     */
    private Faction findWeakestFaction(Faction raider, List<Faction> allFactions) {
        return allFactions.stream()
                .filter(f -> f.getId() != raider.getId())
                .filter(Faction::isActive)
                .filter(f -> f.getGold() > 0)
                .min(Comparator.comparingInt(Faction::getStrength))
                .orElse(null);
    }

    /**
     * Utility: Weighted random choice
     */
    private FactionAction weightedRandomChoice(Map<FactionAction, Integer> weights) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int randomValue = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (Map.Entry<FactionAction, Integer> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                return entry.getKey();
            }
        }

        return FactionAction.GATHER_RESOURCES; // Fallback
    }
}