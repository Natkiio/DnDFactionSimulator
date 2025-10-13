package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.List;

public class PlayerInfluencePanel extends VBox {

    private DatabaseManager db;
    private ComboBox<String> factionCombo;
    private List<Faction> allFactions;

    public PlayerInfluencePanel(DatabaseManager db) {
        this.db = db;

        setPadding(new Insets(20));
        setSpacing(20);

        // Header
        Label header = new Label("🧙 Player Influence - DM Controls");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Manually intervene in the world simulation");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        // Faction selector
        HBox selectorBox = new HBox(10);
        selectorBox.setStyle("-fx-alignment: center-left;");

        Label selectLabel = new Label("Select Faction:");
        factionCombo = new ComboBox<>();
        factionCombo.setPrefWidth(250);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setOnAction(e -> loadFactions());

        selectorBox.getChildren().addAll(selectLabel, factionCombo, refreshBtn);

        // Action sections
        TitledPane resourcePane = createResourceModificationPane();
        TitledPane eventPane = createEventCreationPane();
        TitledPane relationshipPane = createRelationshipPane();

        Accordion accordion = new Accordion(resourcePane, eventPane, relationshipPane);
        accordion.setExpandedPane(resourcePane);

        getChildren().addAll(header, subtitle, selectorBox, accordion);

        // Initial load
        loadFactions();
    }

    private void loadFactions() {
        allFactions = db.getAllFactions();
        factionCombo.getItems().clear();

        for (Faction f : allFactions) {
            String status = f.isActive() ? "✅" : "⏸️";
            factionCombo.getItems().add(status + " " + f.getName() + " (ID: " + f.getId() + ")");
        }

        if (!allFactions.isEmpty()) {
            factionCombo.getSelectionModel().selectFirst();
        }
    }

    private Faction getSelectedFaction() {
        int index = factionCombo.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < allFactions.size()) {
            return allFactions.get(index);
        }
        return null;
    }

    private TitledPane createResourceModificationPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Gold modification
        HBox goldBox = new HBox(10);
        goldBox.setStyle("-fx-alignment: center-left;");
        Label goldLabel = new Label("💰 Gold:");
        Spinner<Integer> goldSpinner = new Spinner<>(-1000, 1000, 0, 10);
        goldSpinner.setEditable(true);
        goldSpinner.setPrefWidth(100);
        Button applyGoldBtn = new Button("Apply");
        applyGoldBtn.setOnAction(e -> modifyResource("gold", goldSpinner.getValue()));
        goldBox.getChildren().addAll(goldLabel, goldSpinner, applyGoldBtn);

        // Troops modification
        HBox troopsBox = new HBox(10);
        troopsBox.setStyle("-fx-alignment: center-left;");
        Label troopsLabel = new Label("🛡️ Troops:");
        Spinner<Integer> troopsSpinner = new Spinner<>(-500, 500, 0, 5);
        troopsSpinner.setEditable(true);
        troopsSpinner.setPrefWidth(100);
        Button applyTroopsBtn = new Button("Apply");
        applyTroopsBtn.setOnAction(e -> modifyResource("troops", troopsSpinner.getValue()));
        troopsBox.getChildren().addAll(troopsLabel, troopsSpinner, applyTroopsBtn);

        // Magic modification
        HBox magicBox = new HBox(10);
        magicBox.setStyle("-fx-alignment: center-left;");
        Label magicLabel = new Label("🔮 Magic:");
        Spinner<Integer> magicSpinner = new Spinner<>(-100, 100, 0, 5);
        magicSpinner.setEditable(true);
        magicSpinner.setPrefWidth(100);
        Button applyMagicBtn = new Button("Apply");
        applyMagicBtn.setOnAction(e -> modifyResource("magic", magicSpinner.getValue()));
        magicBox.getChildren().addAll(magicLabel, magicSpinner, applyMagicBtn);

        // Influence modification
        HBox influenceBox = new HBox(10);
        influenceBox.setStyle("-fx-alignment: center-left;");
        Label influenceLabel = new Label("📈 Influence:");
        Spinner<Integer> influenceSpinner = new Spinner<>(-100, 100, 0, 5);
        influenceSpinner.setEditable(true);
        influenceSpinner.setPrefWidth(100);
        Button applyInfluenceBtn = new Button("Apply");
        applyInfluenceBtn.setOnAction(e -> modifyResource("influence", influenceSpinner.getValue()));
        influenceBox.getChildren().addAll(influenceLabel, influenceSpinner, applyInfluenceBtn);

        content.getChildren().addAll(
                new Label("Modify faction resources (+ or -):"),
                goldBox, troopsBox, magicBox, influenceBox
        );

        TitledPane pane = new TitledPane("Resource Modification", content);
        return pane;
    }

    private TitledPane createEventCreationPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        Label instructionLabel = new Label("Create a custom world event:");

        TextArea eventDescriptionArea = new TextArea();
        eventDescriptionArea.setPromptText("Describe what happens... (e.g., 'The kingdom discovers a dragon's lair')");
        eventDescriptionArea.setPrefHeight(100);
        eventDescriptionArea.setWrapText(true);

        ComboBox<FactionAction> actionCombo = new ComboBox<>();
        actionCombo.getItems().addAll(FactionAction.values());
        actionCombo.setValue(FactionAction.GATHER_RESOURCES);

        Button createEventBtn = new Button("📝 Create Event");
        createEventBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        createEventBtn.setOnAction(e -> {
            Faction faction = getSelectedFaction();
            String description = eventDescriptionArea.getText().trim();

            if (faction == null) {
                showAlert("No Faction Selected", "Please select a faction first.");
                return;
            }

            if (description.isEmpty()) {
                showAlert("No Description", "Please enter an event description.");
                return;
            }

            WorldEvent event = new WorldEvent(
                    db.getCurrentTurn(),
                    faction.getId(),
                    actionCombo.getValue(),
                    description
            );

            if (db.addWorldEvent(event)) {
                showSuccess("Event Created", "Custom event added to the world log!");
                eventDescriptionArea.clear();
            }
        });

        content.getChildren().addAll(instructionLabel, eventDescriptionArea,
                new Label("Action Type:"), actionCombo, createEventBtn);

        return new TitledPane("Create Custom Event", content);
    }

    private TitledPane createRelationshipPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        Label instructionLabel = new Label("Force a relationship between factions:");

        ComboBox<String> targetFactionCombo = new ComboBox<>();
        targetFactionCombo.setPrefWidth(250);

        Button refreshTargetBtn = new Button("🔄 Load Targets");
        refreshTargetBtn.setOnAction(e -> {
            targetFactionCombo.getItems().clear();
            for (Faction f : db.getAllFactions()) {
                targetFactionCombo.getItems().add(f.getName() + " (ID: " + f.getId() + ")");
            }
        });

        HBox targetBox = new HBox(10);
        targetBox.getChildren().addAll(new Label("Target Faction:"), targetFactionCombo, refreshTargetBtn);

        ComboBox<RelationshipType> relationshipTypeCombo = new ComboBox<>();
        relationshipTypeCombo.getItems().addAll(RelationshipType.values());
        relationshipTypeCombo.setValue(RelationshipType.NEUTRAL);

        Spinner<Integer> strengthSpinner = new Spinner<>(-100, 100, 0, 10);
        strengthSpinner.setEditable(true);

        HBox strengthBox = new HBox(10);
        strengthBox.getChildren().addAll(new Label("Strength (-100 to +100):"), strengthSpinner);

        Button createRelBtn = new Button("🤝 Create Relationship");
        createRelBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        createRelBtn.setOnAction(e -> {
            Faction faction1 = getSelectedFaction();
            int targetIndex = targetFactionCombo.getSelectionModel().getSelectedIndex();

            if (faction1 == null || targetIndex < 0) {
                showAlert("Invalid Selection", "Please select both factions.");
                return;
            }

            Faction faction2 = db.getAllFactions().get(targetIndex);

            if (faction1.getId() == faction2.getId()) {
                showAlert("Same Faction", "Cannot create relationship with itself!");
                return;
            }

            Relationship rel = new Relationship(
                    faction1.getId(),
                    faction2.getId(),
                    relationshipTypeCombo.getValue(),
                    strengthSpinner.getValue()
            );

            if (db.addRelationship(rel)) {
                showSuccess("Relationship Created",
                        faction1.getName() + " and " + faction2.getName() +
                                " are now " + relationshipTypeCombo.getValue());
            }
        });

        content.getChildren().addAll(
                instructionLabel,
                targetBox,
                new Label("Relationship Type:"),
                relationshipTypeCombo,
                strengthBox,
                createRelBtn
        );

        return new TitledPane("Manage Relationships", content);
    }

    private void modifyResource(String resourceType, int amount) {
        Faction faction = getSelectedFaction();
        if (faction == null) {
            showAlert("No Faction Selected", "Please select a faction first.");
            return;
        }

        switch (resourceType) {
            case "gold":
                faction.addGold(amount);
                break;
            case "troops":
                faction.addTroops(amount);
                break;
            case "magic":
                faction.addMagic(amount);
                break;
            case "influence":
                faction.addInfluence(amount);
                break;
        }

        if (db.updateFaction(faction)) {
            String sign = amount >= 0 ? "+" : "";
            showSuccess("Resource Modified",
                    faction.getName() + " " + resourceType + " " + sign + amount);

            // Log as event
            WorldEvent event = new WorldEvent(
                    db.getCurrentTurn(),
                    faction.getId(),
                    FactionAction.GATHER_RESOURCES,
                    "DM intervention: " + faction.getName() + " " + resourceType +
                            " modified by " + sign + amount
            );
            db.addWorldEvent(event);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}