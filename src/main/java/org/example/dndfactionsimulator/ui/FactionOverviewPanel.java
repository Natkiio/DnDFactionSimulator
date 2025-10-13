package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.List;

public class FactionOverviewPanel extends VBox {

    private DatabaseManager db;
    private TableView<Faction> factionTable;
    private Label statsLabel;

    public FactionOverviewPanel(DatabaseManager db) {
        this.db = db;

        setPadding(new Insets(10));
        setSpacing(10);

        // Header
        Label header = new Label("👥 Faction Overview");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Buttons Row 1
        HBox buttonBox1 = new HBox(10);
        buttonBox1.setStyle("-fx-alignment: center-left;");

        Button addBtn = new Button("➕ Add Faction");
        addBtn.setOnAction(e -> addFaction());

        Button editBtn = new Button("✏️ Edit Selected");
        editBtn.setOnAction(e -> editSelected());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> refreshFactions());

        buttonBox1.getChildren().addAll(addBtn, editBtn, refreshBtn);

        // Buttons Row 2 (Danger zone)
        HBox buttonBox2 = new HBox(10);
        buttonBox2.setStyle("-fx-alignment: center-left;");

        Button inactiveBtn = new Button("⏸️ Mark Inactive");
        inactiveBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        inactiveBtn.setOnAction(e -> markInactive());

        Button reactivateBtn = new Button("▶️ Reactivate");
        reactivateBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        reactivateBtn.setOnAction(e -> reactivateFaction());

        Button deleteBtn = new Button("🗑️ Permanently Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> permanentlyDelete());

        Label dangerLabel = new Label("⚠️ Danger Zone:");
        dangerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        buttonBox2.getChildren().addAll(dangerLabel, inactiveBtn, reactivateBtn, deleteBtn);

        // Table
        factionTable = new TableView<>();
        factionTable.setPrefHeight(400);

        TableColumn<Faction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Faction, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<Faction, FactionType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        TableColumn<Faction, Alignment> alignmentCol = new TableColumn<>("Alignment");
        alignmentCol.setCellValueFactory(new PropertyValueFactory<>("alignment"));
        alignmentCol.setPrefWidth(120);

        TableColumn<Faction, Integer> goldCol = new TableColumn<>("Gold");
        goldCol.setCellValueFactory(new PropertyValueFactory<>("gold"));
        goldCol.setPrefWidth(70);

        TableColumn<Faction, Integer> troopsCol = new TableColumn<>("Troops");
        troopsCol.setCellValueFactory(new PropertyValueFactory<>("troops"));
        troopsCol.setPrefWidth(70);

        TableColumn<Faction, Integer> magicCol = new TableColumn<>("Magic");
        magicCol.setCellValueFactory(new PropertyValueFactory<>("magic"));
        magicCol.setPrefWidth(70);

        TableColumn<Faction, Integer> influenceCol = new TableColumn<>("Influence");
        influenceCol.setCellValueFactory(new PropertyValueFactory<>("influence"));
        influenceCol.setPrefWidth(80);

        TableColumn<Faction, Integer> strengthCol = new TableColumn<>("Strength");
        strengthCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStrength()).asObject());
        strengthCol.setPrefWidth(80);

        TableColumn<Faction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isActive() ? "✅ Active" : "⏸️ Inactive"));
        statusCol.setPrefWidth(80);

        factionTable.getColumns().addAll(idCol, nameCol, typeCol, alignmentCol,
                goldCol, troopsCol, magicCol, influenceCol, strengthCol, statusCol);

        // Stats
        statsLabel = new Label();
        statsLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(header, buttonBox1, buttonBox2, factionTable, statsLabel);

        // Initial load
        refreshFactions();
    }

    public void refreshFactions() {
        factionTable.getItems().clear();
        List<Faction> factions = db.getAllFactions();
        factionTable.getItems().addAll(factions);

        int active = (int) factions.stream().filter(Faction::isActive).count();
        int inactive = (int) factions.stream().filter(f -> !f.isActive()).count();
        int totalStrength = factions.stream().filter(Faction::isActive)
                .mapToInt(Faction::getStrength).sum();

        statsLabel.setText(String.format("Total: %d | Active: %d | Inactive: %d | Combined Strength: %d",
                factions.size(), active, inactive, totalStrength));
    }

    private void addFaction() {
        FactionDialog dialog = new FactionDialog();
        dialog.showAndWait().ifPresent(faction -> {
            if (db.addFaction(faction)) {
                refreshFactions();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Faction created successfully!");
                alert.showAndWait();
            }
        });
    }

    private void editSelected() {
        Faction selected = factionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a faction to edit.");
            alert.showAndWait();
            return;
        }

        // Create a simple edit dialog
        Dialog<Faction> dialog = new Dialog<>();
        dialog.setTitle("Edit Faction");
        dialog.setHeaderText("Modify faction stats for: " + selected.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> goldSpinner = new Spinner<>(0, 10000, selected.getGold(), 10);
        goldSpinner.setEditable(true);

        Spinner<Integer> troopsSpinner = new Spinner<>(0, 10000, selected.getTroops(), 10);
        troopsSpinner.setEditable(true);

        Spinner<Integer> magicSpinner = new Spinner<>(0, 1000, selected.getMagic(), 5);
        magicSpinner.setEditable(true);

        Spinner<Integer> influenceSpinner = new Spinner<>(0, 1000, selected.getInfluence(), 5);
        influenceSpinner.setEditable(true);

        grid.add(new Label("Gold:"), 0, 0);
        grid.add(goldSpinner, 1, 0);
        grid.add(new Label("Troops:"), 0, 1);
        grid.add(troopsSpinner, 1, 1);
        grid.add(new Label("Magic:"), 0, 2);
        grid.add(magicSpinner, 1, 2);
        grid.add(new Label("Influence:"), 0, 3);
        grid.add(influenceSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setGold(goldSpinner.getValue());
                selected.setTroops(troopsSpinner.getValue());
                selected.setMagic(magicSpinner.getValue());
                selected.setInfluence(influenceSpinner.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(faction -> {
            if (db.updateFaction(faction)) {
                refreshFactions();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Faction updated successfully!");
                alert.showAndWait();
            }
        });
    }

    private void markInactive() {
        Faction selected = factionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a faction to mark as inactive.");
            return;
        }

        if (!selected.isActive()) {
            showWarning("Already Inactive", "This faction is already inactive.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark Inactive");
        confirm.setHeaderText("Mark " + selected.getName() + " as inactive?");
        confirm.setContentText("This preserves the faction in history but marks it as no longer active in the world.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                selected.setActive(false);
                if (db.updateFaction(selected)) {
                    refreshFactions();
                    showSuccess("Faction Inactive", selected.getName() + " has been marked as inactive.");
                }
            }
        });
    }

    private void reactivateFaction() {
        Faction selected = factionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a faction to reactivate.");
            return;
        }

        if (selected.isActive()) {
            showWarning("Already Active", "This faction is already active.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reactivate Faction");
        confirm.setHeaderText("Reactivate " + selected.getName() + "?");
        confirm.setContentText("This will bring the faction back into the active world.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                selected.setActive(true);
                if (db.updateFaction(selected)) {
                    refreshFactions();
                    showSuccess("Faction Reactivated", selected.getName() + " is now active again!");
                }
            }
        });
    }

    private void permanentlyDelete() {
        Faction selected = factionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a faction to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("⚠️ PERMANENT DELETE");
        confirm.setHeaderText("PERMANENTLY delete " + selected.getName() + "?");
        confirm.setContentText(
                "This will:\n" +
                        "• Remove the faction from the database forever\n" +
                        "• Orphan any relationships this faction has\n" +
                        "• Orphan any events involving this faction\n\n" +
                        "This CANNOT be undone!\n\n" +
                        "Use this only for factions created by mistake."
        );

        ButtonType deleteButton = new ButtonType("DELETE FOREVER", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteButton, cancelButton);

        confirm.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                // Add a second confirmation for safety
                Alert doubleCheck = new Alert(Alert.AlertType.CONFIRMATION);
                doubleCheck.setTitle("Are you SURE?");
                doubleCheck.setHeaderText("Final confirmation");
                doubleCheck.setContentText("Type the faction name to confirm deletion:\n" + selected.getName());

                TextField confirmField = new TextField();
                confirmField.setPromptText("Type faction name here");

                doubleCheck.getDialogPane().setContent(confirmField);

                doubleCheck.showAndWait().ifPresent(finalResponse -> {
                    if (finalResponse == ButtonType.OK && confirmField.getText().equals(selected.getName())) {
                        if (db.deleteFaction(selected.getId())) {
                            refreshFactions();
                            showSuccess("Deleted", selected.getName() + " has been permanently removed from the database.");
                        } else {
                            showError("Delete Failed", "Could not delete faction from database.");
                        }
                    } else {
                        showWarning("Cancelled", "Name did not match. Deletion cancelled.");
                    }
                });
            }
        });
    }

    // Helper methods for alerts
    private void showWarning(String title, String content) {
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}