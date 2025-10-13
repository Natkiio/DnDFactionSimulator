package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.dndfactionsimulator.model.*;

public class FactionDialog extends Dialog<Faction> {

    private TextField nameField;
    private ComboBox<FactionType> typeCombo;
    private ComboBox<Alignment> alignmentCombo;
    private Spinner<Integer> goldSpinner;
    private Spinner<Integer> troopsSpinner;
    private Spinner<Integer> magicSpinner;
    private Spinner<Integer> influenceSpinner;

    public FactionDialog() {
        setTitle("Create New Faction");
        setHeaderText("Enter faction details");

        // Create the dialog pane buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Name field
        nameField = new TextField();
        nameField.setPromptText("Faction Name");

        // Type dropdown
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(FactionType.values());
        typeCombo.setValue(FactionType.KINGDOM);

        // Alignment dropdown
        alignmentCombo = new ComboBox<>();
        alignmentCombo.getItems().addAll(Alignment.values());
        alignmentCombo.setValue(Alignment.TRUE_NEUTRAL);

        // Resource spinners
        goldSpinner = new Spinner<>(0, 1000, 100, 10);
        goldSpinner.setEditable(true);

        troopsSpinner = new Spinner<>(0, 500, 50, 5);
        troopsSpinner.setEditable(true);

        magicSpinner = new Spinner<>(0, 100, 10, 5);
        magicSpinner.setEditable(true);

        influenceSpinner = new Spinner<>(0, 100, 20, 5);
        influenceSpinner.setEditable(true);

        // Add to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);

        grid.add(new Label("Alignment:"), 0, 2);
        grid.add(alignmentCombo, 1, 2);

        grid.add(new Label("Gold:"), 0, 3);
        grid.add(goldSpinner, 1, 3);

        grid.add(new Label("Troops:"), 0, 4);
        grid.add(troopsSpinner, 1, 4);

        grid.add(new Label("Magic:"), 0, 5);
        grid.add(magicSpinner, 1, 5);

        grid.add(new Label("Influence:"), 0, 6);
        grid.add(influenceSpinner, 1, 6);

        getDialogPane().setContent(grid);

        // Request focus on name field
        javafx.application.Platform.runLater(() -> nameField.requestFocus());

        // Convert result when Create button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (nameField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Faction name cannot be empty!");
                    alert.showAndWait();
                    return null;
                }

                Faction faction = new Faction(
                        nameField.getText().trim(),
                        typeCombo.getValue(),
                        alignmentCombo.getValue()
                );

                faction.setGold(goldSpinner.getValue());
                faction.setTroops(troopsSpinner.getValue());
                faction.setMagic(magicSpinner.getValue());
                faction.setInfluence(influenceSpinner.getValue());

                return faction;
            }
            return null;
        });
    }
}