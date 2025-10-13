package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.List;

public class EventLogPanel extends VBox {

    private DatabaseManager db;
    private ListView<String> eventListView;
    private ComboBox<String> filterCombo;
    private Label statsLabel;

    public EventLogPanel(DatabaseManager db) {
        this.db = db;

        setPadding(new Insets(10));
        setSpacing(10);

        // Header
        Label header = new Label("📜 World Event Log");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setStyle("-fx-alignment: center-left;");

        Label filterLabel = new Label("Filter:");
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Events", "Current Turn", "Last 5 Turns");
        filterCombo.setValue("All Events");
        filterCombo.setOnAction(e -> refreshEvents());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> refreshEvents());

        filterBox.getChildren().addAll(filterLabel, filterCombo, refreshBtn);

        // Event list
        eventListView = new ListView<>();
        eventListView.setPrefHeight(400);

        // Stats label
        statsLabel = new Label("Total Events: 0");
        statsLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(header, filterBox, eventListView, statsLabel);

        // Initial load
        refreshEvents();
    }

    public void refreshEvents() {
        eventListView.getItems().clear();

        List<WorldEvent> events = db.getAllEvents();

        // Apply filter
        String filter = filterCombo.getValue();
        int currentTurn = db.getCurrentTurn();

        if (filter.equals("Current Turn")) {
            events = events.stream()
                    .filter(e -> e.getTurnNumber() == currentTurn)
                    .toList();
        } else if (filter.equals("Last 5 Turns")) {
            events = events.stream()
                    .filter(e -> e.getTurnNumber() >= currentTurn - 5)
                    .toList();
        }

        // Display events
        for (WorldEvent event : events) {
            String emoji = getActionEmoji(event.getAction());
            String eventText = String.format("%s [Turn %d] %s - %s",
                    emoji,
                    event.getTurnNumber(),
                    event.getAction().getDisplayName(),
                    event.getDescription());
            eventListView.getItems().add(eventText);
        }

        statsLabel.setText("Total Events: " + events.size());
    }

    private String getActionEmoji(FactionAction action) {
        return switch (action) {
            case ATTACK -> "⚔️";
            case RAID -> "🗡️";
            case GATHER_RESOURCES -> "💰";
            case RECRUIT_TROOPS -> "🛡️";
            case STUDY_MAGIC -> "🔮";
            case EXPAND_INFLUENCE -> "📈";
            case FORM_ALLIANCE -> "🤝";
            case BREAK_ALLIANCE -> "💔";
            case TRADE -> "💱";
            case FORTIFY -> "🏰";
            case SPY -> "🕵️";
            case SABOTAGE -> "💣";
            case INTERNAL_DECAY -> "☠️";
        };
    }
}